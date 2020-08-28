package dev.nokee.buildadapter.cmake.internal.plugins;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import dev.nokee.buildadapter.cmake.internal.GitBasedGroupIdSupplier;
import dev.nokee.buildadapter.cmake.internal.fileapi.*;
import dev.nokee.buildadapter.cmake.internal.tasks.CMakeMSBuildAdapterTask;
import dev.nokee.buildadapter.cmake.internal.tasks.CMakeMakeAdapterTask;
import dev.nokee.core.exec.CommandLineTool;
import dev.nokee.platform.nativebase.internal.ConfigurationUtils;
import dev.nokee.runtime.base.internal.tools.ToolRepository;
import dev.nokee.runtime.nativebase.internal.locators.CmakeLocator;
import dev.nokee.runtime.nativebase.internal.locators.MSBuildLocator;
import dev.nokee.runtime.nativebase.internal.locators.MakeLocator;
import dev.nokee.runtime.nativebase.internal.locators.VswhereLocator;
import lombok.SneakyThrows;
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
import java.util.stream.Collectors;

import static dev.nokee.buildadapter.cmake.internal.DeferUtils.asToStringObject;

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

		val client = new CodeModelClientImpl(() -> CommandLineTool.of(repository.findAll("cmake").iterator().next().getPath()));
		val replyFiles = client.query(settings.getRootDir());


		replyFiles.visit(new CodeModelReplyFiles.Visitor() {
			CodeModel.Configuration configuration = null;

			@Override
			public void visit(CodeModel codemodel) {
				// TODO: This should be abstracted out from the codemodel
				val cmakeFileApiDirectory = new File(settings.getSettingsDir(), ".cmake/api/v1");
				val cmakeFileApiReplyDirectory = new File(cmakeFileApiDirectory, "reply");

				if (codemodel.getConfigurations().isEmpty()) {
					System.err.println("No CMake configuration found!");
				} else if (codemodel.getConfigurations().size() == 1) {
					configuration = codemodel.getConfigurations().iterator().next();
				} else {
					configuration = codemodel.getConfigurations().stream().filter(it -> it.getName().toLowerCase().equals("release")).findFirst().orElse(null);
					if (configuration == null) {
						System.err.println(String.format("No CMake release configuration found, possible choices was: '%s'.", codemodel.getConfigurations().stream().map(CodeModel.Configuration::getName).collect(Collectors.joining("','"))));
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
								val targetModel = new Gson().fromJson(FileUtils.readFileToString(new File(cmakeFileApiReplyDirectory, target.getJsonFile()), Charset.defaultCharset()), CodeModelTarget.class);
								val configurationUtils = project.getObjects().newInstance(ConfigurationUtils.class);
								if (!targetModel.getType().equals("STATIC_LIBRARY")) {
									project.getLogger().error(String.format("Unsupported target type of '%s' on project '%s', supported target type is 'STATIC_LIBRARY'.", targetModel.getType(), project.getPath()));
									return;
								}

								var compileAction = configurationUtils.asOutgoingHeaderSearchPathFrom();
								// A typical static library
								if (!targetModel.getCompileGroups().isEmpty()) {
									val compileGroup = targetModel.getCompileGroups().iterator().next(); // Assuming only one
									compileAction = compileAction.headerDirectoryArtifacts(compileGroup.getIncludes().stream().map(CodeModelTarget.CompileGroup.Include::getPath).map(rootProject::file).collect(Collectors.toList()));
								} else { // We may have a header-only library in the form of a "fake" target to overcome the "pseudo-target limitation".
									// Find the lowest common folder of all headers in the sources
									// TODO: use C/C++ headers extensions from UTType
									val includePath = targetModel.getSources().stream().map(CodeModelTarget.Source::getPath).filter(it -> it.endsWith(".h")).map(it -> it.substring(0, FilenameUtils.indexOfLastSeparator(it))).reduce((a, b) -> {
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
								if (!targetModel.getCompileGroups().isEmpty()) {
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

			@Override
			public void visit(CodeModelTarget codeModelTarget) {

			}

			@Override
			public void visit(Index replyIndex) {

			}
		});
	}

	@Inject
	protected abstract ProviderFactory getProviders();

	private void configureProjectGroup(Project project) {
		project.setGroup(asToStringObject(new GitBasedGroupIdSupplier(project.getRootDir())));
	}
}
