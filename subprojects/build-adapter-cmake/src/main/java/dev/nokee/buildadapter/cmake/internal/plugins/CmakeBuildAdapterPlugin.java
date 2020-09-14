package dev.nokee.buildadapter.cmake.internal.plugins;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import dev.nokee.buildadapter.cmake.internal.GitBasedGroupIdSupplier;
import dev.nokee.buildadapter.cmake.internal.tasks.CMakeMSBuildAdapterTask;
import dev.nokee.buildadapter.cmake.internal.tasks.CMakeMakeAdapterTask;
import dev.nokee.core.exec.CommandLineTool;
import dev.nokee.core.exec.LoggingEngine;
import dev.nokee.core.exec.ProcessBuilderEngine;
import dev.nokee.platform.nativebase.internal.ConfigurationUtils;
import dev.nokee.runtime.base.internal.tools.ToolRepository;
import dev.nokee.runtime.nativebase.internal.locators.CmakeLocator;
import dev.nokee.runtime.nativebase.internal.locators.MSBuildLocator;
import dev.nokee.runtime.nativebase.internal.locators.MakeLocator;
import dev.nokee.runtime.nativebase.internal.locators.VswhereLocator;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.val;
import lombok.var;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.initialization.Settings;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.internal.os.OperatingSystem;

import javax.inject.Inject;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static dev.nokee.utils.DeferUtils.asToStringObject;

public abstract class CmakeBuildAdapterPlugin implements Plugin<Settings> {
	@SneakyThrows
	@Override
	public void apply(Settings settings) {
		val repository = new ToolRepository();
		repository.register("cmake", new CmakeLocator());
		repository.register("make", new MakeLocator());
		repository.register("msbuild", new MSBuildLocator(() -> CommandLineTool.of(repository.findAll("vswhere").iterator().next().getPath())));
		repository.register("vswhere", new VswhereLocator());

		settings.getGradle().allprojects(this::configureProjectGroup);

		// Query CMake using file API
		val cmakeFileApiDirectory = new File(settings.getSettingsDir(), ".cmake/api/v1");
		val cmakeFileApiQueryCodemodelFile = new File(cmakeFileApiDirectory, "query/codemodel-v2");
		val cmakeFileApiReplyDirectory = new File(cmakeFileApiDirectory, "reply");

		// TODO: check if Gradle already reset the files
		//  if so, maybe we should do an out of source build to ensure build artifacts are kept
		if (cmakeFileApiDirectory.exists()) {
			FileUtils.deleteDirectory(cmakeFileApiDirectory);
		}

		// Create query file
		cmakeFileApiQueryCodemodelFile.getParentFile().mkdirs();
		cmakeFileApiQueryCodemodelFile.createNewFile();

		// TODO: Check cmake tool can be found
		// TODO: Check version supports File API: starting with cmake 3.14 before the feature doesn't exists
		CommandLineTool.of(repository.findAll("cmake").iterator().next().getPath()).withArguments(".").newInvocation().workingDirectory(settings.getSettingsDir()).buildAndSubmit(LoggingEngine.wrap(new ProcessBuilderEngine())).waitFor().assertNormalExitValue();

		File replyFile = null;
		try (val stream = Files.newDirectoryStream(cmakeFileApiReplyDirectory.toPath(), this::findCodemodelReplyFile)) {
			val iter = stream.iterator();
			if (!iter.hasNext()) {
				throw new IllegalStateException("No response from cmake");
			}

			replyFile = iter.next().toFile();
		}

		val codemodel = new Gson().fromJson(FileUtils.readFileToString(replyFile, Charset.defaultCharset()), CodemodelV2.class);

		CodemodelV2.Configuration configuration = null;
		if (codemodel.getConfigurations().isEmpty()) {
			System.err.println("No CMake configuration found!");
		} else if (codemodel.getConfigurations().size() == 1) {
			configuration = codemodel.getConfigurations().iterator().next();
		} else {
			configuration = codemodel.getConfigurations().stream().filter(it -> it.getName().toLowerCase().equals("release")).findFirst().orElse(null);
			if (configuration == null) {
				System.err.println(String.format("No CMake release configuration found, possible choices was: '%s'.", codemodel.getConfigurations().stream().map(CodemodelV2.Configuration::getName).collect(Collectors.joining("','"))));
			}
		}

		val configurationName = configuration.getName();
		configuration.getTargets().forEach(target -> {
			settings.include(target.getName());
			settings.getGradle().rootProject(rootProject -> {
				rootProject.project(target.getName(), new Action<Project>() {
					@SneakyThrows
					@Override
					public void execute(Project project) {
						val targetModel = new Gson().fromJson(FileUtils.readFileToString(new File(cmakeFileApiReplyDirectory, target.getJsonFile()), Charset.defaultCharset()), CodemodelTarget.class);
						val configurationUtils = project.getObjects().newInstance(ConfigurationUtils.class);
						if (!targetModel.getType().equals("STATIC_LIBRARY")) {
							project.getLogger().error(String.format("Unsupported target type of '%s' on project '%s', supported target type is 'STATIC_LIBRARY'.", targetModel.getType(), project.getPath()));
							return;
						}

						var compileAction = configurationUtils.asOutgoingHeaderSearchPathFrom();
						// A typical static library
						if (targetModel.getCompileGroups() != null) {
							val compileGroup = targetModel.getCompileGroups().iterator().next(); // Assuming only one
							compileAction = compileAction.headerDirectoryArtifacts(compileGroup.getIncludes().stream().map(CodemodelTarget.CompileGroup.Include::getPath).map(rootProject::file).collect(Collectors.toList()));
						} else { // We may have a header-only library in the form of a "fake" target to overcome the "pseudo-target limitation".
							// Find the lowest common folder of all headers in the sources
							// TODO: use C/C++ headers extensions from UTType
							val includePath = targetModel.getSources().stream().map(CodemodelTarget.Source::getPath).filter(it -> it.endsWith(".h")).map(it -> it.substring(0, FilenameUtils.indexOfLastSeparator(it))).reduce((a, b) -> {
								if (a.length() < b.length()) {
									return a;
								}
								return b;
							}).<String>map(it -> it.substring(0, FilenameUtils.indexOfLastSeparator(it)));
							System.out.println(includePath.get());
							compileAction = compileAction.headerDirectoryArtifacts(ImmutableList.of(includePath.map(rootProject::file).get()));
						}

						project.getConfigurations().create("compileElements", compileAction);

						// Compile only if there are compile groups
						if (targetModel.getCompileGroups() != null) {
							if (OperatingSystem.current().isWindows()) {
								val makeTask = project.getTasks().register("make", CMakeMSBuildAdapterTask.class, task -> {
									task.getTargetName().set(targetModel.getName());
									task.getConfigurationName().set(configurationName);
									task.getBuiltFile().set(rootProject.file(targetModel.getArtifacts().iterator().next().getPath()));
									task.getWorkingDirectory().set(rootProject.getLayout().getProjectDirectory());
									task.getMsbuildTool().set(getProviders().provider(() -> CommandLineTool.of(repository.findAll("msbuild").iterator().next().getPath())));
								});
								project.getConfigurations().create("linkElements", configurationUtils.asOutgoingLinkLibrariesFrom().staticLibraryArtifact(makeTask.flatMap(CMakeMSBuildAdapterTask::getBuiltFile)));
							} else {
								val makeTask = project.getTasks().register("make", CMakeMakeAdapterTask.class, task -> {
									task.getTargetName().set(targetModel.getName());
									task.getBuiltFile().set(rootProject.file(targetModel.getArtifacts().iterator().next().getPath()));
									task.getWorkingDirectory().set(rootProject.getLayout().getProjectDirectory());
									task.getMakeTool().set(getProviders().provider(() -> CommandLineTool.of(repository.findAll("make").iterator().next().getPath())));
								});
								project.getConfigurations().create("linkElements", configurationUtils.asOutgoingLinkLibrariesFrom().staticLibraryArtifact(makeTask.flatMap(CMakeMakeAdapterTask::getBuiltFile)));
							}
						} else {
							project.getConfigurations().create("linkElements", configurationUtils.asOutgoingLinkLibrariesFrom());
						}


						project.getConfigurations().create("runtimeElements", configurationUtils.asOutgoingRuntimeLibrariesFrom());
					}
				});
			});
		});
	}

	@Inject
	protected abstract ProviderFactory getProviders();

	private boolean findCodemodelReplyFile(Path entry) {
		// Skipping directly to codemodel reply file.
		// Ideally, we would read the index file.
		return entry.getFileName().toString().startsWith("codemodel-v2");
	}

	private void configureProjectGroup(Project project) {
		project.setGroup(asToStringObject(new GitBasedGroupIdSupplier(project.getRootDir())));
	}

	@Value
	private static class CodemodelV2 {
		List<Configuration> configurations;

		@Value
		public static class Configuration {
			String name;
			List<Target> targets;

			@Value
			public static class Target {
				String jsonFile;
				String name;
			}
		}
	}

	@Value
	private static class CodemodelTarget {
		List<Artifact> artifacts;

		@Value
		public static class Artifact {
			String path;
		}

		List<CompileGroup> compileGroups;

		@Value
		public static class CompileGroup {
			List<Include> includes;

			@Value
			public static class Include {
				String path;
			}
		}

		List<Source> sources;

		@Value
		public static class Source {
			String path;
		}

		String name;

		// Possible values: STATIC_LIBRARY
		String type;
	}
}
