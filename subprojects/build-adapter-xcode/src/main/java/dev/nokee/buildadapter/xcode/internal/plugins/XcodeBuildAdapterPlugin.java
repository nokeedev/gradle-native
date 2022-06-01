/*
 * Copyright 2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.nokee.buildadapter.xcode.internal.plugins;

import dev.nokee.xcode.XCProject;
import dev.nokee.xcode.XCProjectReference;
import dev.nokee.xcode.XCWorkspace;
import dev.nokee.xcode.XCWorkspaceReference;
import lombok.val;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.initialization.Settings;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.provider.ValueSource;
import org.gradle.api.provider.ValueSourceParameters;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.nio.file.Path;

import static dev.nokee.buildadapter.xcode.internal.plugins.HasWorkingDirectory.workingDirectory;
import static dev.nokee.platform.base.internal.util.PropertyUtils.set;
import static dev.nokee.utils.ActionUtils.composite;
import static dev.nokee.utils.ProviderUtils.forUseAtConfigurationTime;
import static dev.nokee.utils.TaskUtils.temporaryDirectoryPath;

class XcodeBuildAdapterPlugin implements Plugin<Settings> {
	private static final Logger LOGGER = Logging.getLogger(XcodeBuildAdapterPlugin.class);
	private final ProviderFactory providers;

	@Inject
	public XcodeBuildAdapterPlugin(ProviderFactory providers) {
		this.providers = providers;
	}

	@Override
	public void apply(Settings settings) {
		settings.getGradle().rootProject(new RedirectProjectBuildDirectoryToRootBuildDirectory());

		val allWorkspaceLocations = forUseAtConfigurationTime(providers.of(AllXCWorkspaceLocationsValueSource.class, it -> it.parameters(p -> p.getSearchDirectory().set(settings.getSettingsDir()))));
		val selectedWorkspaceLocation = allWorkspaceLocations.map(new SelectXCWorkspaceLocationTransformation());

		val workspace = forUseAtConfigurationTime(providers.of(XCWorkspaceDataValueSource.class, it -> it.parameters(p -> p.getWorkspace().set(selectedWorkspaceLocation)))).getOrNull();
		if (workspace == null) {
			settings.getGradle().rootProject(rootProject -> {
				val allProjectLocations = forUseAtConfigurationTime(providers.of(AllXCProjectLocationsValueSource.class, it -> it.parameters(p -> p.getSearchDirectory().set(settings.getSettingsDir()))));
				val selectedProjectLocation = allProjectLocations.map(new SelectXCProjectLocationTransformation());

				val project = selectedProjectLocation.getOrNull();
				if (project == null) {
					LOGGER.warn(String.format("The plugin 'dev.nokee.xcode-build-adapter' has no effect on project '%s' because no Xcode workspace or project were found in '%s'. See https://nokee.fyi/using-xcode-build-adapter for more details.", settings.getGradle(), settings.getSettingsDir()));
				} else {
					LOGGER.quiet("Taking this project " + project.getLocation());
					forXcodeProject(project).execute(rootProject);
				}
			});
		} else {
			for (XCProjectReference project : workspace.getProjectLocations()) {
				// TODO: What happen if a workspace reference project in parent directory? It would break the project mapping.
				val relativePath = settings.getSettingsDir().toPath().relativize(project.getLocation());
				val projectPath = asProjectPath(relativePath);
				LOGGER.info(String.format("Mapping Xcode project '%s' to Gradle project '%s'.", relativePath, projectPath));
				settings.include(projectPath);
				settings.getGradle().rootProject(rootProject -> {
					rootProject.project(projectPath, forXcodeProject(project, composite(
						workingDirectory(set(rootProject.getLayout().getProjectDirectory())),
						(XcodebuildExecTask task) -> task.getSdk().set(providers.environmentVariable("XCODE_SDK")),
						(XcodebuildExecTask task) -> task.getConfiguration().set(providers.environmentVariable("XCODE_BUILD_TYPE"))
					)));
				});
			}
			settings.getGradle().rootProject(forXcodeWorkspace(workspace));
		}
	}

	private String asProjectPath(Path relativePath) {
		return FilenameUtils.separatorsToUnix(FilenameUtils.removeExtension(relativePath.toString())).replace('/', ':');
	}

	private static Action<Project> forXcodeProject(XCProjectReference reference) {
		return forXcodeProject(reference, __ -> {});
	}

	private static Action<Project> forXcodeProject(XCProjectReference reference, Action<? super XcodebuildExecTask> action) {
		return project -> {
			val xcodeProject = forUseAtConfigurationTime(project.getProviders().of(XCProjectDataValueSource.class, it -> it.getParameters().getProject().set(reference))).get();
			xcodeProject.getTargetNames().forEach(targetName -> {
				project.getTasks().register(targetName, XcodeTargetExecTask.class, task -> {
					task.setGroup("Xcode Target");
					task.getProjectLocation().set(reference.getLocation().toFile());
					task.getTargetName().set(targetName);
					task.getDerivedDataPath().set(project.getLayout().getBuildDirectory().dir(temporaryDirectoryPath(task) + "/derivedData"));
					task.getOutputDirectory().set(project.getLayout().getBuildDirectory().dir("derivedData/" + targetName));
					action.execute(task);
				});
			});
			xcodeProject.getSchemeNames().forEach(schemeName -> {
				project.getTasks().register("build" + StringUtils.capitalize(schemeName), XcodeProjectSchemeExecTask.class, task -> {
					task.setGroup("Xcode Scheme");
					task.getProjectLocation().set(reference.getLocation().toFile());
					task.getSchemeName().set(schemeName);
					task.getDerivedDataPath().set(project.getLayout().getBuildDirectory().dir(temporaryDirectoryPath(task) + "/derivedData"));
					action.execute(task);
				});
			});
		};
	}

	private static Action<Project> forXcodeWorkspace(XCWorkspace workspace) {
		return project -> {
			workspace.getSchemeNames().forEach(schemeName -> {
				project.getTasks().register("build" + StringUtils.capitalize(schemeName), XcodeSchemeExecTask.class, task -> {
					task.setGroup("Xcode Scheme");
					task.getWorkspaceLocation().set(workspace.getLocation().toFile());
					task.getSchemeName().set(schemeName);
					task.getDerivedDataPath().set(project.getRootProject().getLayout().getBuildDirectory().dir("derivedData"));
				});
			});
		};
	}

	public static abstract class XCWorkspaceDataValueSource implements ValueSource<XCWorkspace, XCWorkspaceDataValueSource.Parameters> {
		interface Parameters extends ValueSourceParameters {
			Property<XCWorkspaceReference> getWorkspace();
		}

		@Nullable
		@Override
		public XCWorkspace obtain() {
			if (getParameters().getWorkspace().isPresent()) {
				return getParameters().getWorkspace().get().load();
			} else {
				return null;
			}
		}
	}

	public static abstract class XCProjectDataValueSource implements ValueSource<XCProject, XCProjectDataValueSource.Parameters> {
		interface Parameters extends ValueSourceParameters {
			Property<XCProjectReference> getProject();
		}

		@Nullable
		@Override
		public XCProject obtain() {
			if (getParameters().getProject().isPresent()) {
				return getParameters().getProject().get().load();
			} else {
				return null;
			}
		}
	}
}
