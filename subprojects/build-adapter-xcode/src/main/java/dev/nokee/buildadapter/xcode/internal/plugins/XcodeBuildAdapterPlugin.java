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

import com.google.common.collect.ImmutableMap;
import dev.nokee.buildadapter.xcode.internal.DefaultGradleProjectPathService;
import dev.nokee.buildadapter.xcode.internal.DefaultXCProjectReferenceToStringer;
import dev.nokee.buildadapter.xcode.internal.GradleBuildLayout;
import dev.nokee.buildadapter.xcode.internal.components.GradleProjectPathComponent;
import dev.nokee.buildadapter.xcode.internal.components.GradleProjectTag;
import dev.nokee.buildadapter.xcode.internal.components.GradleSettingsTag;
import dev.nokee.buildadapter.xcode.internal.components.SettingsDirectoryComponent;
import dev.nokee.buildadapter.xcode.internal.components.XCProjectComponent;
import dev.nokee.buildadapter.xcode.internal.components.XCTargetComponent;
import dev.nokee.buildadapter.xcode.internal.components.XCTargetTaskComponent;
import dev.nokee.buildadapter.xcode.internal.rules.XCProjectDescriptionRule;
import dev.nokee.buildadapter.xcode.internal.rules.XCTargetTaskDescriptionRule;
import dev.nokee.buildadapter.xcode.internal.rules.XCTargetTaskGroupRule;
import dev.nokee.buildadapter.xcode.internal.rules.XcodeBuildLayoutRule;
import dev.nokee.buildadapter.xcode.internal.rules.XcodeProjectPathRule;
import dev.nokee.buildadapter.xcode.internal.rules.XcodeProjectsDiscoveryRule;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelComponentReference;
import dev.nokee.model.internal.core.ModelNodes;
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
import dev.nokee.utils.ActionUtils;
import dev.nokee.xcode.XCFileReference;
import dev.nokee.xcode.XCProject;
import dev.nokee.xcode.XCProjectReference;
import dev.nokee.xcode.XCWorkspace;
import dev.nokee.xcode.XCWorkspaceReference;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Transformer;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.api.attributes.Usage;
import org.gradle.api.initialization.Settings;
import org.gradle.api.internal.GradleInternal;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.provider.ValueSource;
import org.gradle.api.provider.ValueSourceParameters;
import org.gradle.api.services.BuildServiceRegistration;
import org.gradle.build.event.BuildEventsListenerRegistry;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
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
import static dev.nokee.utils.TransformerUtils.transformEach;

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

		val listenerRegistry = ((GradleInternal) settings.getGradle()).getServices().get(BuildEventsListenerRegistry.class);
		val service = registerBuildServiceIfAbsent(settings.getGradle(), XCLoaderService.class);
		listenerRegistry.onTaskCompletion(service);
		service.get(); // hypothetically, make sure the service clear the cache even if configuration phase fails

		settings.getExtensions().getByType(ModelConfigurer.class).configure(new XcodeBuildLayoutRule(GradleBuildLayout.forSettings(settings), providers));
		settings.getExtensions().getByType(ModelConfigurer.class).configure(new XcodeProjectPathRule(new DefaultGradleProjectPathService(settings.getSettingsDir().toPath())));
		settings.getExtensions().getByType(ModelConfigurer.class).configure(new XcodeProjectsDiscoveryRule(settings.getExtensions().getByType(ModelRegistry.class), objects, providers));

		val settingsEntity = settings.getExtensions().getByType(ModelLookup.class).get(ModelPath.root());
		settingsEntity.addComponent(new SettingsDirectoryComponent(settings.getSettingsDir().toPath()));
		settingsEntity.addComponent(tag(GradleSettingsTag.class));

		registerBuildServiceIfAbsent(settings, XcodeDependenciesService.class, it -> {
			it.getProjectReferences().putAll(providers.provider(() -> {
				return ImmutableMap.copyOf(settings.getExtensions().getByType(ModelLookup.class).query(entity -> entity.has(GradleProjectPathComponent.class) && entity.has(XCProjectComponent.class)).map(entity -> {
					return new HashMap.SimpleImmutableEntry<>(entity.get(XCProjectComponent.class).get(), entity.get(GradleProjectPathComponent.class).get().toString());
				}));
			}));
		});
	}

	public static Action<Project> forXcodeProject(XCProjectReference reference, Action<? super XcodebuildExecTask> action) {
		return project -> {
			project.getPluginManager().apply(ComponentModelBasePlugin.class);

			project.getExtensions().getByType(ModelConfigurer.class).configure(new XCTargetTaskDescriptionRule());
			project.getExtensions().getByType(ModelConfigurer.class).configure(new XCTargetTaskGroupRule());
			project.getExtensions().getByType(ModelConfigurer.class).configure(new XCProjectDescriptionRule(new DefaultXCProjectReferenceToStringer(project.getRootDir().toPath())));

			@SuppressWarnings("unchecked")
			final Provider<XcodeDependenciesService> service = project.getProviders().provider(() -> (BuildServiceRegistration<XcodeDependenciesService, XcodeDependenciesService.Parameters>) project.getGradle().getSharedServices().getRegistrations().findByName(XcodeDependenciesService.class.getSimpleName())).flatMap(BuildServiceRegistration::getService);

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
								return target.load().getInputFiles().stream().map(it::forFile).filter(Objects::nonNull).collect(Collectors.toList());
							}).map(transformEach(asDependency(project)))
						)).orElse(Collections.emptyList()));
						configuration.getDependencies().addAllLater(finalizeValueOnRead(project.getObjects().listProperty(Dependency.class).value(service.map(it -> {
								return target.load().getDependencies().stream().map(it::forTarget).filter(Objects::nonNull).collect(Collectors.toList());
							}).map(transformEach(asDependency(project)))
						)).orElse(Collections.emptyList()));
					});

				val targetTask = project.getExtensions().getByType(ModelRegistry.class).register(DomainObjectEntities.newEntity(TaskName.lifecycle(), XcodeTargetExecTask.class, it -> it.ownedBy(entity)))
					.as(XcodeTargetExecTask.class)
					.configure(task -> {
						task.getXcodeProject().set(reference);
						task.getTargetName().set(target.getName());
						task.getDerivedDataPath().set(project.getLayout().getBuildDirectory().dir(temporaryDirectoryPath(task) + "/derivedData"));
						task.getOutputDirectory().set(project.getLayout().getBuildDirectory().dir("derivedData/" + target.getName()));
						task.getXcodeInstallation().set(project.getProviders().of(CurrentXcodeInstallationValueSource.class, ActionUtils.doNothing()));
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
				entity.addComponent(new XCTargetTaskComponent(ModelNodes.of(targetTask)));

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

			val projectEntity = project.getExtensions().getByType(ModelLookup.class).get(ModelPath.root());
			projectEntity.addComponent(new XCProjectComponent(reference));
			projectEntity.addComponent(tag(GradleProjectTag.class));
		};
	}

	public static Transformer<Dependency, XcodeDependenciesService.Coordinate> asDependency(Project project) {
		return it -> {
			val dep = (ProjectDependency) project.getDependencies().create(project.project(it.projectPath.toString()));
			dep.capabilities(capabilities -> {
				capabilities.requireCapability("net.nokeedev.xcode:" + it.projectName + "-" + it.capabilityName + ":1.0");
			});
			return dep;
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
