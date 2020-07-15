package dev.nokee.buildadapter.cmake.internal.plugins;

import com.google.gson.Gson;
import dev.nokee.buildadapter.cmake.internal.DeferUtils;
import dev.nokee.buildadapter.cmake.internal.GitBasedGroupIdSupplier;
import dev.nokee.buildadapter.cmake.internal.tasks.CMakeMakeAdapterTask;
import dev.nokee.core.exec.CommandLine;
import dev.nokee.core.exec.LoggingEngine;
import dev.nokee.core.exec.ProcessBuilderEngine;
import dev.nokee.platform.nativebase.internal.ConfigurationUtils;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.val;
import lombok.var;
import org.apache.commons.io.FileUtils;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.initialization.Settings;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static dev.nokee.buildadapter.cmake.internal.DeferUtils.asToStringObject;

public abstract class CmakeBuildAdapterPlugin implements Plugin<Settings> {
	@SneakyThrows
	@Override
	public void apply(Settings settings) {
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

		CommandLine.of("cmake", ".").newInvocation().workingDirectory(settings.getSettingsDir()).buildAndSubmit(LoggingEngine.wrap(new ProcessBuilderEngine())).waitFor().assertNormalExitValue();

		File replyFile = null;
		try (val stream = Files.newDirectoryStream(cmakeFileApiReplyDirectory.toPath(), this::findCodemodelReplyFile)) {
			val iter = stream.iterator();
			if (!iter.hasNext()) {
				throw new IllegalStateException("No response from cmake");
			}

			replyFile = iter.next().toFile();
		}

		val codemodel = new Gson().fromJson(FileUtils.readFileToString(replyFile, Charset.defaultCharset()), CodemodelV2.class);

		codemodel.getConfigurations().forEach(configuration -> {
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
							val compileGroup = targetModel.getCompileGroups().iterator().next(); // Assuming only one
							compileAction = compileAction.headerDirectoryArtifacts(compileGroup.getIncludes().stream().map(CodemodelTarget.CompileGroup.Include::getPath).map(rootProject::file).collect(Collectors.toList()));

							project.getConfigurations().create("compileElements", compileAction);

							val makeTask = project.getTasks().register("make", CMakeMakeAdapterTask.class, task -> {
								task.getTargetName().set(targetModel.getName());
								task.getBuiltFile().set(rootProject.file(targetModel.getArtifacts().iterator().next().getPath()));
								task.getWorkingDirectory().set(rootProject.getLayout().getProjectDirectory());
							});

							project.getConfigurations().create("linkElements", configurationUtils.asOutgoingLinkLibrariesFrom().staticLibraryArtifact(makeTask.flatMap(CMakeMakeAdapterTask::getBuiltFile)));
							project.getConfigurations().create("runtimeElements", configurationUtils.asOutgoingRuntimeLibrariesFrom());
						}
					});
				});
			});
		});
	}

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

		String name;

		// Possible values: STATIC_LIBRARY
		String type;
	}
}
