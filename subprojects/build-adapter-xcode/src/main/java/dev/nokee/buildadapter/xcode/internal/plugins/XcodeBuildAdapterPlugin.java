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

import dev.nokee.utils.ActionUtils;
import dev.nokee.xcode.XCFileReference;
import dev.nokee.xcode.XCProject;
import dev.nokee.xcode.XCProjectReference;
import dev.nokee.xcode.XCTarget;
import dev.nokee.xcode.XCTargetReference;
import dev.nokee.xcode.XCWorkspace;
import dev.nokee.xcode.XCWorkspaceReference;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.api.attributes.Usage;
import org.gradle.api.initialization.Settings;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.provider.ValueSource;
import org.gradle.api.provider.ValueSourceParameters;
import org.gradle.api.services.BuildServiceRegistration;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static dev.nokee.buildadapter.xcode.internal.plugins.HasWorkingDirectory.workingDirectory;
import static dev.nokee.platform.base.internal.util.PropertyUtils.set;
import static dev.nokee.utils.ActionUtils.composite;
import static dev.nokee.utils.ProviderUtils.finalizeValueOnRead;
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

		forUseAtConfigurationTime(settings.getGradle().getSharedServices().registerIfAbsent("loader", XCLoaderService.class, ActionUtils.doNothing())).get();

		val allWorkspaceLocations = forUseAtConfigurationTime(providers.of(AllXCWorkspaceLocationsValueSource.class, it -> it.parameters(p -> p.getSearchDirectory().set(settings.getSettingsDir()))));
		val selectedWorkspaceLocation = allWorkspaceLocations.map(new SelectXCWorkspaceLocationTransformation());

		val workspace = forUseAtConfigurationTime(providers.of(XCWorkspaceDataValueSource.class, it -> it.parameters(p -> {
			p.getWorkspace().set(selectedWorkspaceLocation);
		}))).getOrNull();
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
			val service = forUseAtConfigurationTime(settings.getGradle().getSharedServices().registerIfAbsent("implicitDependencies", XcodeImplicitDependenciesService.class, spec -> {
				spec.parameters(parameters -> {
					parameters.getLocation().set(workspace.toReference());
				});
			}));
			for (XCProjectReference project : workspace.getProjectLocations()) {
				val projectPath = service.get().asProjectPath(project);
				settings.include(projectPath);
				settings.getGradle().rootProject(rootProject -> {
					rootProject.project(projectPath, forXcodeProject(project, composite(
						workingDirectory(set(rootProject.getLayout().getProjectDirectory())),
						(XcodebuildExecTask task) -> task.getSdk().set(providers.environmentVariable("XCODE_SDK")),
						(XcodebuildExecTask task) -> task.getConfiguration().set(providers.environmentVariable("XCODE_BUILD_TYPE"))
					)));
				});
			}
			settings.getGradle().rootProject(forXcodeWorkspace(workspace, composite(
				(XcodebuildExecTask task) -> task.getSdk().set(providers.environmentVariable("XCODE_SDK")),
				(XcodebuildExecTask task) -> task.getConfiguration().set(providers.environmentVariable("XCODE_BUILD_TYPE"))
			)));
		}
	}

	private static Action<Project> forXcodeProject(XCProjectReference reference) {
		return forXcodeProject(reference, __ -> {});
	}

	private static Stream<XCFileReference> allInputFiles(XCTarget target) {
		return Stream.concat(target.getInputFiles().stream(), target.getDependencies().stream().map(XCTargetReference::load).flatMap(XcodeBuildAdapterPlugin::allInputFiles));
	}

	private static Action<Project> forXcodeProject(XCProjectReference reference, Action<? super XcodebuildExecTask> action) {
		return project -> {
			@SuppressWarnings("unchecked")
			final Provider<XcodeImplicitDependenciesService> service = project.getProviders().provider(() -> (BuildServiceRegistration<XcodeImplicitDependenciesService, XcodeImplicitDependenciesService.Parameters>) project.getGradle().getSharedServices().getRegistrations().findByName("implicitDependencies")).flatMap(BuildServiceRegistration::getService);
			val xcodeProject = forUseAtConfigurationTime(project.getProviders().of(XCProjectDataValueSource.class, it -> it.parameters(p -> {
				p.getProject().set(reference);
			}))).get();
			xcodeProject.getTargets().forEach(target -> {
				val derivedData = project.getConfigurations().create(target.getName() + "DerivedData", configuration -> {
					configuration.setCanBeConsumed(false);
					configuration.setCanBeResolved(true);
					configuration.attributes(attributes -> {
						attributes.attribute(Usage.USAGE_ATTRIBUTE, project.getObjects().named(Usage.class, "xcode-derived-data"));
					});
					configuration.getDependencies().addAllLater(finalizeValueOnRead(project.getObjects().listProperty(Dependency.class).value(service.map(it -> {
						return allInputFiles(target.load()).map(it::findTarget).filter(Objects::nonNull).map(t -> {
							val dep = (ProjectDependency) project.getDependencies().create(project.project(":" + it.asProjectPath(t.getProject())));
							dep.capabilities(capabilities -> {
								capabilities.requireCapability("net.nokeedev.xcode:" + t.getName() + ":1.0");
							});
							return dep;
						}).collect(Collectors.toList());
					}))).orElse(Collections.emptyList()));
				});

				val targetTask = project.getTasks().register(target.getName(), XcodeTargetExecTask.class, task -> {
					task.setGroup("Xcode Target");
					task.getXcodeProject().set(reference);
					task.getTargetName().set(target.getName());
					task.getDerivedDataPath().set(project.getLayout().getBuildDirectory().dir(temporaryDirectoryPath(task) + "/derivedData"));
					task.getOutputDirectory().set(project.getLayout().getBuildDirectory().dir("derivedData/" + target.getName()));
					task.getInputDerivedData().from(derivedData);
					task.getInputFiles().from((Callable<Object>) () -> allInputFiles(target.load()).filter(it -> it.getType() != XCFileReference.XCFileType.BUILT_PRODUCT).map(it -> it.resolve(new XCFileReference.ResolveContext() {
						@Override
						public Path getSourceRoot() {
							return reference.getLocation().getParent();
						}

						@Override
						public Path getBuiltProductDirectory() {
							// TODO: The following is only an approximation of what the BUILT_PRODUCT_DIR would be, use -showBuildSettings
							// TODO: Guard against the missing derived data path
							// TODO: We should map derived data path as a collection of build settings via helper method
							return task.getDerivedDataPath().dir("Build/Products/" + task.getConfiguration().get() + "-" + task.getSdk().get()).get().getAsFile().toPath();
						}

						@Override
						public Path getSdkRoot() {
							// TODO: Use -showBuildSettings to get SDKROOT value (or we could guess it)
							return task.getSdk().map(it -> {
								if (it.toLowerCase(Locale.ENGLISH).equals("iphoneos")) {
									return new File("/Applications/Xcode.app/Contents/Developer/Platforms/iPhoneOS.platform/Developer/SDKs/iPhoneOS.sdk").toPath();
								}
								return null;
							}).get();
						}

						@Override
						public Path getDeveloperDirectory() {
							// TODO: Use -showBuildSettings to get DEVELOPER_DIR value (or we could guess it)
							return new File("/Applications/Xcode.app/Contents/Developer").toPath();
						}
					})).collect(Collectors.toList()));
					task.getInputFiles().finalizeValueOnRead();
					action.execute(task);
				});

				project.getConfigurations().create(target.getName() + "Elements", configuration -> {
					configuration.setCanBeConsumed(true);
					configuration.setCanBeResolved(false);
					configuration.attributes(attributes -> {
						attributes.attribute(Usage.USAGE_ATTRIBUTE, project.getObjects().named(Usage.class, "xcode-derived-data"));
					});
					configuration.outgoing(outgoing -> {
						outgoing.capability("net.nokeedev.xcode:" + target.getName() + ":1.0");
						outgoing.artifact(targetTask.flatMap(XcodeTargetExecTask::getOutputDirectory));
					});
				});
			});
			xcodeProject.getSchemeNames().forEach(schemeName -> {
				project.getTasks().register("build" + StringUtils.capitalize(schemeName), XcodeProjectSchemeExecTask.class, task -> {
					task.setGroup("Xcode Scheme");
					task.getXcodeProject().set(reference);
					task.getSchemeName().set(schemeName);
					task.getDerivedDataPath().set(project.getLayout().getBuildDirectory().dir(temporaryDirectoryPath(task) + "/derivedData"));
					action.execute(task);
				});
			});
		};
	}

	private static Action<Project> forXcodeWorkspace(XCWorkspace workspace, Action<? super XcodebuildExecTask> action) {
		return project -> {
			workspace.getSchemeNames().forEach(schemeName -> {
				project.getTasks().register("build" + StringUtils.capitalize(schemeName), XcodeSchemeExecTask.class, task -> {
					task.setGroup("Xcode Scheme");
					task.getXcodeWorkspace().set(workspace.toReference());
					task.getSchemeName().set(schemeName);
					task.getDerivedDataPath().set(project.getRootProject().getLayout().getBuildDirectory().dir("derivedData"));
					action.execute(task);
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
