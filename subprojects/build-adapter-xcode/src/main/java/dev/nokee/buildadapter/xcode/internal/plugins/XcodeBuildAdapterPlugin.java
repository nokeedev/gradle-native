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

import dev.nokee.buildadapter.xcode.internal.DefaultGradleProjectPathService;
import dev.nokee.buildadapter.xcode.internal.GradleBuildLayout;
import dev.nokee.buildadapter.xcode.internal.GradleProjectPathService;
import dev.nokee.buildadapter.xcode.internal.components.GradleSettingsTag;
import dev.nokee.buildadapter.xcode.internal.components.SettingsDirectoryComponent;
import dev.nokee.buildadapter.xcode.internal.components.XCProjectComponent;
import dev.nokee.buildadapter.xcode.internal.rules.XcodeBuildLayoutRule;
import dev.nokee.buildadapter.xcode.internal.rules.XcodeProjectPathRule;
import dev.nokee.buildadapter.xcode.internal.rules.XcodeProjectsDiscoveryRule;
import dev.nokee.buildadapter.xcode.internal.rules.XcodeWorkspaceRule;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelComponentReference;
import dev.nokee.model.internal.core.ModelPath;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.core.ParentComponent;
import dev.nokee.model.internal.names.ElementNameComponent;
import dev.nokee.model.internal.registry.ModelConfigurer;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.internal.DomainObjectEntities;
import dev.nokee.platform.base.internal.dependencies.ConsumableDependencyBucketSpec;
import dev.nokee.platform.base.internal.dependencies.ResolvableDependencyBucketSpec;
import dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin;
import dev.nokee.platform.base.internal.plugins.OnDiscover;
import dev.nokee.platform.base.internal.tasks.TaskName;
import dev.nokee.xcode.XCFileReference;
import dev.nokee.xcode.XCProject;
import dev.nokee.xcode.XCProjectReference;
import dev.nokee.xcode.XCWorkspace;
import dev.nokee.xcode.XCWorkspaceReference;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.api.attributes.Usage;
import org.gradle.api.initialization.Settings;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.model.ObjectFactory;
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

import static dev.nokee.model.internal.tags.ModelTags.tag;
import static dev.nokee.utils.BuildServiceUtils.registerBuildServiceIfAbsent;
import static dev.nokee.utils.ProviderUtils.finalizeValueOnRead;
import static dev.nokee.utils.ProviderUtils.forParameters;
import static dev.nokee.utils.ProviderUtils.forUseAtConfigurationTime;
import static dev.nokee.utils.TaskUtils.temporaryDirectoryPath;

public class XcodeBuildAdapterPlugin implements Plugin<Settings> {
	private static final Logger LOGGER = Logging.getLogger(XcodeBuildAdapterPlugin.class);
	private final ProviderFactory providers;
	private final ObjectFactory objects;

	@Inject
	public XcodeBuildAdapterPlugin(ProviderFactory providers, ObjectFactory objects) {
		this.providers = providers;
		this.objects = objects;
	}

	@Override
	public void apply(Settings settings) {
		settings.getPluginManager().apply("dev.nokee.model-base");
		settings.getPluginManager().apply("dev.nokee.cocoapods-support");
		settings.getGradle().rootProject(new RedirectProjectBuildDirectoryToRootBuildDirectory());

		forUseAtConfigurationTime(registerBuildServiceIfAbsent(settings.getGradle(), XCLoaderService.class)).get();

		settings.getExtensions().getByType(ModelConfigurer.class).configure(new XcodeBuildLayoutRule(GradleBuildLayout.forSettings(settings), providers));
		settings.getExtensions().getByType(ModelConfigurer.class).configure(new XcodeProjectPathRule(new DefaultGradleProjectPathService(settings.getSettingsDir().toPath())));
		settings.getExtensions().getByType(ModelConfigurer.class).configure(new XcodeProjectsDiscoveryRule(settings.getExtensions().getByType(ModelRegistry.class), objects, providers));
		settings.getExtensions().getByType(ModelConfigurer.class).configure(new XcodeWorkspaceRule(settings, providers));

		val settingsEntity = settings.getExtensions().getByType(ModelLookup.class).get(ModelPath.root());
		settingsEntity.addComponent(new SettingsDirectoryComponent(settings.getSettingsDir().toPath()));
		settingsEntity.addComponent(tag(GradleSettingsTag.class));
	}

	public static Action<Project> forXcodeProject(XCProjectReference reference, Action<? super XcodebuildExecTask> action) {
		return project -> {
			project.getPluginManager().apply(ComponentModelBasePlugin.class);

			@SuppressWarnings("unchecked")
			final Provider<XcodeImplicitDependenciesService> service = project.getProviders().provider(() -> (BuildServiceRegistration<XcodeImplicitDependenciesService, XcodeImplicitDependenciesService.Parameters>) project.getGradle().getSharedServices().getRegistrations().findByName(XcodeImplicitDependenciesService.class.getSimpleName())).flatMap(BuildServiceRegistration::getService);
			val projectPathService = new DefaultGradleProjectPathService(project.getRootDir().toPath());

			project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelComponentReference.of(XCProjectComponent.class), (entity, xcProject) -> {
				val xcodeProject = forUseAtConfigurationTime(project.getProviders().of(XCProjectDataValueSource.class, forParameters(it -> {
					it.getProject().set(reference);
				}))).get();
				xcodeProject.getTargets().forEach(target -> {
					project.getExtensions().getByType(ModelRegistry.class).register(ModelRegistration.builder()
						.withComponent(new ElementNameComponent(target.getName()))
						.withComponent(new XCTargetComponent(target))
						.withComponent(new ParentComponent(entity))
						.build());
				});
				project.getExtensions().getByType(ModelRegistry.class).register(DomainObjectEntities.newEntity(TaskName.of("inspect"), InspectXcodeTask.class, it -> it.ownedBy(entity)))
					.as(InspectXcodeTask.class)
					.configure(task -> {
						task.getXcodeProject().set(reference);
					});
			}));

			project.getExtensions().getByType(ModelConfigurer.class).configure(new OnDiscover(ModelActionWithInputs.of(ModelComponentReference.of(XCTargetComponent.class), (entity, xcTarget) -> {
				val target = xcTarget.get();

				val derivedData = project.getExtensions().getByType(ModelRegistry.class).register(DomainObjectEntities.newEntity("derivedData", ResolvableDependencyBucketSpec.class, it -> it.ownedBy(entity)))
					.as(Configuration.class)
					.configure(configuration -> {
						configuration.attributes(attributes -> {
							attributes.attribute(Usage.USAGE_ATTRIBUTE, project.getObjects().named(Usage.class, "xcode-derived-data"));
						});
					})
					.configure(configuration -> {
						configuration.getDependencies().addAllLater(finalizeValueOnRead(project.getObjects().listProperty(Dependency.class).value(service.map(it -> {
							return target.load().getInputFiles().stream().map(it::findTarget).filter(Objects::nonNull).map(t -> {
								val dep = (ProjectDependency) project.getDependencies().create(project.project(":" + it.asProjectPath(t.getProject())));
								dep.capabilities(capabilities -> {
									capabilities.requireCapability("net.nokeedev.xcode:" + t.getProject().getName() + "-" + t.getName() + ":1.0");
								});
								return dep;
							}).collect(Collectors.toList());
						}).orElse(Collections.emptyList()))));
						configuration.getDependencies().addAllLater(finalizeValueOnRead(project.getObjects().listProperty(Dependency.class).value(project.provider(() -> {
							return target.load().getDependencies().stream().map(t -> {
								val dep = (ProjectDependency) project.getDependencies().create(project.project(projectPathService.toProjectPath(t.getProject()).toString()));
								dep.capabilities(capabilities -> {
									capabilities.requireCapability("net.nokeedev.xcode:" + t.getProject().getName() + "-" + t.getName() + ":1.0");
								});
								return dep;
							}).collect(Collectors.toList());
						}).orElse(Collections.emptyList()))));
					});

				val targetTask = project.getExtensions().getByType(ModelRegistry.class).register(DomainObjectEntities.newEntity(TaskName.lifecycle(), XcodeTargetExecTask.class, it -> it.ownedBy(entity)))
					.as(XcodeTargetExecTask.class)
					.configure(task -> {
						task.setGroup("Xcode Target");
						task.getXcodeProject().set(reference);
						task.getTargetName().set(target.getName());
						task.getDerivedDataPath().set(project.getLayout().getBuildDirectory().dir(temporaryDirectoryPath(task) + "/derivedData"));
						task.getOutputDirectory().set(project.getLayout().getBuildDirectory().dir("derivedData/" + target.getName()));
						task.getInputDerivedData().from(derivedData);
						task.getInputFiles().from((Callable<Object>) () -> target.load().getInputFiles().stream().filter(it -> it.getType() != XCFileReference.XCFileType.BUILT_PRODUCT).map(it -> it.resolve(new XCFileReference.ResolveContext() {
							@Override
							public Path getBuiltProductDirectory() {
								// TODO: The following is only an approximation of what the BUILT_PRODUCT_DIR would be, use -showBuildSettings
								// TODO: Guard against the missing derived data path
								// TODO: We should map derived data path as a collection of build settings via helper method
								return task.getDerivedDataPath().dir("Build/Products/" + task.getConfiguration().get() + "-" + task.getSdk().get()).get().getAsFile().toPath();
							}

							@Override
							public Path get(String name) {
								switch (name) {
									case "DEVELOPER_DIR":
										// TODO: Use -showBuildSettings to get DEVELOPER_DIR value (or we could guess it)
										return new File("/Applications/Xcode.app/Contents/Developer").toPath();
									case "SDKROOT":
										// TODO: Use -showBuildSettings to get SDKROOT value (or we could guess it)
										return task.getSdk().map(it -> {
											if (it.toLowerCase(Locale.ENGLISH).equals("iphoneos")) {
												return new File("/Applications/Xcode.app/Contents/Developer/Platforms/iPhoneOS.platform/Developer/SDKs/iPhoneOS.sdk").toPath();
											} else if (it.toLowerCase(Locale.ENGLISH).equals("macosx")) {
												return new File("/Applications/Xcode.app/Contents/Developer/Platforms/MacOSX.platform/Developer/SDKs/MacOSX.sdk").toPath();
											} else if (it.toLowerCase(Locale.ENGLISH).equals("iphonesimulator")) {
												return new File("/Applications/Xcode.app/Contents/Developer/Platforms/iPhoneSimulator.platform/Developer/SDKs/iPhoneSimulator.sdk").toPath();
											}
											return null;
										})
											// FIXME: Use -showBuildSettings to get default SDKROOT
											.orElse(new File("/Applications/Xcode.app/Contents/Developer/Platforms/MacOSX.platform/Developer/SDKs/MacOSX.sdk").toPath()).get();
									case "SOURCE_ROOT":
										return reference.getLocation().getParent();
									default:
										return new File(task.getBuildSettings().get().get(name)).toPath();
								}
							}
						})).collect(Collectors.toList()));
						task.getInputFiles().finalizeValueOnRead();
						action.execute(task);
					});

				project.getExtensions().getByType(ModelRegistry.class).register(DomainObjectEntities.newEntity("DerivedDataElements", ConsumableDependencyBucketSpec.class, it -> it.ownedBy(entity)))
					.as(Configuration.class)
					.configure(configuration -> {
						configuration.attributes(attributes -> {
							attributes.attribute(Usage.USAGE_ATTRIBUTE, project.getObjects().named(Usage.class, "xcode-derived-data"));
						});
					})
					.configure(configuration -> {
						configuration.outgoing(outgoing -> {
							outgoing.capability("net.nokeedev.xcode:" + project.getName() + "-" + target.getName() + ":1.0");
							outgoing.artifact(targetTask.flatMap(XcodeTargetExecTask::getOutputDirectory));
						});
					});
			})));
			project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelComponentReference.of(XCProjectComponent.class), (entity, xcProject) -> {
				val xcodeProject = forUseAtConfigurationTime(project.getProviders().of(XCProjectDataValueSource.class, forParameters(it -> {
					it.getProject().set(xcProject.get());
				}))).get();
				xcodeProject.getSchemeNames().forEach(schemeName -> {
					project.getTasks().register("build" + StringUtils.capitalize(schemeName), XcodeProjectSchemeExecTask.class, task -> {
						task.setGroup("Xcode Scheme");
						task.getXcodeProject().set(xcProject.get());
						task.getSchemeName().set(schemeName);
						task.getDerivedDataPath().set(project.getLayout().getBuildDirectory().dir(temporaryDirectoryPath(task) + "/derivedData"));
						action.execute(task);
					});
				});
			}));

			project.getExtensions().getByType(ModelLookup.class).get(ModelPath.root()).addComponent(new XCProjectComponent(reference));
		};
	}

	public static Action<Project> forXcodeWorkspace(XCWorkspace workspace, Action<? super XcodebuildExecTask> action) {
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
		public interface Parameters extends ValueSourceParameters {
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
		public interface Parameters extends ValueSourceParameters {
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
