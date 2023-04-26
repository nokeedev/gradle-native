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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Streams;
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
import dev.nokee.buildadapter.xcode.internal.plugins.vfsoverlay.GenerateVirtualFileSystemOverlaysTask;
import dev.nokee.buildadapter.xcode.internal.plugins.vfsoverlay.MergeVirtualFileSystemOverlaysTask;
import dev.nokee.buildadapter.xcode.internal.plugins.vfsoverlay.VFSOverlayAction;
import dev.nokee.buildadapter.xcode.internal.rules.AttachXCTargetToVariantRule;
import dev.nokee.buildadapter.xcode.internal.rules.TransitionLinkedVariantToRegisterStateRule;
import dev.nokee.buildadapter.xcode.internal.rules.XCProjectDescriptionRule;
import dev.nokee.buildadapter.xcode.internal.rules.XCProjectsDiscoveryRule;
import dev.nokee.buildadapter.xcode.internal.rules.XCTargetComponentDiscoveryRule;
import dev.nokee.buildadapter.xcode.internal.rules.XCTargetTaskDescriptionRule;
import dev.nokee.buildadapter.xcode.internal.rules.XCTargetTaskGroupRule;
import dev.nokee.buildadapter.xcode.internal.rules.XCTargetVariantDiscoveryRule;
import dev.nokee.buildadapter.xcode.internal.rules.XcodeBuildLayoutRule;
import dev.nokee.buildadapter.xcode.internal.rules.XcodeProjectPathRule;
import dev.nokee.model.capabilities.variants.LinkedVariantsComponent;
import dev.nokee.model.capabilities.variants.VariantInformationComponent;
import dev.nokee.model.internal.core.DisplayNameComponent;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelComponentReference;
import dev.nokee.model.internal.core.ModelComponentType;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.core.ModelPath;
import dev.nokee.model.internal.registry.ModelConfigurer;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.model.internal.tags.ModelTags;
import dev.nokee.model.internal.type.ModelType;
import dev.nokee.platform.base.internal.DomainObjectEntities;
import dev.nokee.platform.base.internal.IsComponent;
import dev.nokee.platform.base.internal.dependencies.ConsumableDependencyBucketSpec;
import dev.nokee.platform.base.internal.dependencies.ResolvableDependencyBucketSpec;
import dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin;
import dev.nokee.platform.base.internal.plugins.OnDiscover;
import dev.nokee.platform.base.internal.tasks.TaskName;
import dev.nokee.utils.ActionUtils;
import dev.nokee.utils.TransformerUtils;
import dev.nokee.xcode.XCDependenciesLoader;
import dev.nokee.xcode.XCLoaders;
import dev.nokee.xcode.XCProjectReference;
import dev.nokee.xcode.XCTargetReference;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Transformer;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.api.attributes.Attribute;
import org.gradle.api.attributes.Usage;
import org.gradle.api.file.RegularFile;
import org.gradle.api.initialization.Settings;
import org.gradle.api.internal.GradleInternal;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.build.event.BuildEventsListenerRegistry;
import org.gradle.process.CommandLineArgumentProvider;

import javax.inject.Inject;
import java.io.File;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import static dev.nokee.model.internal.tags.ModelTags.tag;
import static dev.nokee.util.internal.OutOfDateReasonSpec.because;
import static dev.nokee.utils.BuildServiceUtils.registerBuildServiceIfAbsent;
import static dev.nokee.utils.CallableUtils.ofSerializableCallable;
import static dev.nokee.utils.ProviderUtils.finalizeValueOnRead;
import static dev.nokee.utils.ProviderUtils.forUseAtConfigurationTime;
import static dev.nokee.utils.TaskUtils.temporaryDirectoryPath;
import static dev.nokee.utils.TransformerUtils.Transformer.of;
import static dev.nokee.utils.TransformerUtils.toListTransformer;
import static dev.nokee.utils.TransformerUtils.transformEach;

public class XcodeBuildAdapterPlugin implements Plugin<Settings> {
	private static final Logger LOGGER = Logging.getLogger(XcodeBuildAdapterPlugin.class);
	private final ProviderFactory providers;
	private final ObjectFactory objects;
	private final BuildInputService buildInputs;

	@Inject
	public XcodeBuildAdapterPlugin(ProviderFactory providers, ObjectFactory objects) {
		this.providers = providers;
		this.objects = objects;
		this.buildInputs = new BuildInputService(providers);
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

		// This custom locator also capture inputs to the build
		XCProjectLocator locator = toFileTransformer()
			.andThen(buildInputs.capture("find workspaces in default location", new LoadWorkspaceReferencesTransformer(new DefaultXCWorkspaceLocator())))
			.andThen(new SelectSingleXCWorkspaceTransformer())
			.andThen(buildInputs.capture("find projects from selected workspace", of(new LoadWorkspaceProjectReferencesIfAvailableTransformer(new XCProjectSupplier(settings.getSettingsDir().toPath())))
				.andThen(new UnpackCrossProjectReferencesTransformer())))
			.andThen(new WarnOnMissingXCProjectsTransformer(settings.getSettingsDir().toPath()))
			.andThen(toListTransformer())::transform;
		settings.getExtensions().getByType(ModelConfigurer.class).configure(new XCProjectsDiscoveryRule(settings.getExtensions().getByType(ModelRegistry.class), locator));

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

	public static TransformerUtils.Transformer<File, Path> toFileTransformer() {
		return Path::toFile;
	}

	private static Provider<String> fromCommandLine(ProviderFactory providers, String name) {
		// TODO: I'm not convince forUseAtConfigurationTime is required here
		return forUseAtConfigurationTime(providers.systemProperty(name)).orElse(forUseAtConfigurationTime(providers.gradleProperty(name)));
	}

	public static Action<Project> forXcodeProject(XCProjectReference reference, Action<? super XcodebuildExecTask> action) {
		return project -> {
			project.getPluginManager().apply(ComponentModelBasePlugin.class);

			val buildInputs = new BuildInputService(project.getProviders());

			project.getExtensions().getByType(ModelConfigurer.class).configure(new XCTargetTaskDescriptionRule());
			project.getExtensions().getByType(ModelConfigurer.class).configure(new XCTargetTaskGroupRule());
			project.getExtensions().getByType(ModelConfigurer.class).configure(new XCProjectDescriptionRule(new DefaultXCProjectReferenceToStringer(project.getRootDir().toPath())));

			@SuppressWarnings("unchecked")
			final Provider<XcodeDependenciesService> service = (Provider<XcodeDependenciesService>) project.getGradle().getSharedServices().getRegistrations().getByName(XcodeDependenciesService.class.getSimpleName()).getService();

			project.getExtensions().getByType(ModelConfigurer.class).configure(new XCTargetComponentDiscoveryRule(project.getExtensions().getByType(ModelRegistry.class), buildInputs.capture("loads all targets from " + project, (Transformer<Iterable<XCTargetReference>, XCProjectReference> & Serializable) XCLoaders.allTargetsLoader()::load)::transform));
			project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelComponentReference.of(XCProjectComponent.class), (entity, xcProject) -> {
				project.getExtensions().getByType(ModelRegistry.class).register(DomainObjectEntities.newEntity(TaskName.of("inspect"), InspectXcodeTask.class, it -> it.ownedBy(entity)))
					.as(InspectXcodeTask.class)
					.configure(task -> {
						task.getXcodeProject().set(reference);
						task.getXCLoaderService().set(service);
						task.usesService(service);
					});
			}));

			project.getExtensions().getByType(ModelConfigurer.class).configure(new OnDiscover(new XCTargetVariantDiscoveryRule(buildInputs.capture("loads target configurations", (Transformer<Iterable<String>, XCTargetReference> & Serializable) it -> XCLoaders.targetConfigurationsLoader().load(it))::transform, fromCommandLine(project.getProviders(), "configuration")::getOrNull)));
			project.getExtensions().getByType(ModelConfigurer.class).configure(new AttachXCTargetToVariantRule());
			project.getExtensions().getByType(ModelConfigurer.class).configure(new TransitionLinkedVariantToRegisterStateRule());

			project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelComponentReference.of(XCTargetComponent.class), ModelComponentReference.of(VariantInformationComponent.class), (entity, target, variantInfo) -> {
				entity.addComponent(new DisplayNameComponent(String.format("target variant '%s:%s' of %s", target.get().getName(), variantInfo.getName(), project)));
			}));

			project.getExtensions().getByType(ModelConfigurer.class).configure(new OnDiscover(ModelActionWithInputs.of(ModelComponentReference.of(XCTargetComponent.class), ModelTags.referenceOf(IsComponent.class), (entity, target, ignored1) -> {
				val targetLifecycleTask = project.getExtensions().getByType(ModelRegistry.class).register(DomainObjectEntities.newEntity(TaskName.lifecycle(), XcodeTargetLifecycleTask.class, it -> it.ownedBy(entity)))
					.as(XcodeTargetLifecycleTask.class)
					.configure(task -> {
						final XCTargetReference targetReference = target.get();
						task.getConfigurationFlag().convention(project.getProviders().systemProperty("configuration").orElse(project.getProviders().gradleProperty("configuration")).orElse(new DefaultProviderFactory(project.getProviders()).provider(ofSerializableCallable(() -> targetReference.load(XCLoaders.defaultTargetConfigurationLoader())))));
						task.dependsOn((Callable<Object>) task.getConfigurationFlag().map(configuration -> {
							return Streams.stream(ModelStates.finalize(entity).get(LinkedVariantsComponent.class)).filter(it -> it.find(VariantInformationComponent.class).map(t -> t.getName().equals(configuration)).orElse(false)).map(it -> ModelStates.discover(ModelStates.discover(it).get(XCTargetTaskComponent.class).get()).getComponent(ModelComponentType.projectionOf(XcodeTargetExecTask.class)).get(ModelType.of(XcodeTargetExecTask.class))).collect(Collectors.toList());
						})::get);
					});
				entity.addComponent(new XCTargetTaskComponent(ModelNodes.of(targetLifecycleTask)));
			})));

			project.getExtensions().getByType(ModelConfigurer.class).configure(new OnDiscover(ModelActionWithInputs.of(ModelComponentReference.of(XCTargetComponent.class), ModelComponentReference.of(VariantInformationComponent.class), (entity, xcTarget, variantInfo) -> {
				val target = xcTarget.get();

				val derivedData = project.getExtensions().getByType(ModelRegistry.class).register(DomainObjectEntities.newEntity("derivedData", ResolvableDependencyBucketSpec.class, it -> it.ownedBy(entity)))
					.as(Configuration.class)
					.configure(configuration -> {
						configuration.attributes(attributes -> {
							attributes.attribute(Usage.USAGE_ATTRIBUTE, project.getObjects().named(Usage.class, "xcode-derived-data"));
							attributes.attribute(Attribute.of("dev.nokee.xcode.configuration", String.class), variantInfo.getName());
						});
					})
					.configure(configuration -> {
						configuration.getDependencies().addAllLater(finalizeValueOnRead(project.getObjects().listProperty(Dependency.class).value(service.map(it -> {
								return it.load(target).getDependencies().stream().filter(XCDependenciesLoader.CoordinateDependency.class::isInstance).map(dep -> ((XCDependenciesLoader.CoordinateDependency) dep).getCoordinate()).collect(Collectors.toList());
							}).map(transformEach(asDependency(project)))
						)).orElse(Collections.emptyList()));
					});

				val derivedDataTask = project.getExtensions().getByType(ModelRegistry.class).register(DomainObjectEntities.newEntity(TaskName.of("assemble", "derivedDataDir"), AssembleDerivedDataDirectoryTask.class, it -> it.ownedBy(entity)))
					.as(AssembleDerivedDataDirectoryTask.class)
					.configure(task -> {
						task.parameters(parameters -> {
							parameters.getIncomingDerivedDataPaths().from(derivedData);
							parameters.getXcodeDerivedDataPath().set(project.getLayout().getBuildDirectory().dir("tmp-derived-data/" + target.getName() + "-" + variantInfo.getName()));
						});
					});

				val overlays = project.getExtensions().getByType(ModelRegistry.class).register(DomainObjectEntities.newEntity("virtualFileSystemOverlays", ResolvableDependencyBucketSpec.class, it -> it.ownedBy(entity)))
					.as(Configuration.class)
					.configure(configuration -> {
						configuration.attributes(attributes -> {
							attributes.attribute(Usage.USAGE_ATTRIBUTE, project.getObjects().named(Usage.class, "xcode-overlays"));
							attributes.attribute(Attribute.of("dev.nokee.xcode.configuration", String.class), variantInfo.getName());
						});
					})
					.configure(configuration -> {
						configuration.getDependencies().addAllLater(finalizeValueOnRead(project.getObjects().listProperty(Dependency.class).value(service.map(it -> {
								return it.load(target).getDependencies().stream().filter(XCDependenciesLoader.CoordinateDependency.class::isInstance).map(dep -> ((XCDependenciesLoader.CoordinateDependency) dep).getCoordinate()).collect(Collectors.toList());
							}).map(transformEach(asDependency(project)))
						)).orElse(Collections.emptyList()));
					});

				val mergeTask = project.getExtensions().getByType(ModelRegistry.class).register(DomainObjectEntities.newEntity(TaskName.of("merge", "virtualFileSystemOverlays"), MergeVirtualFileSystemOverlaysTask.class, it -> it.ownedBy(entity)))
					.as(MergeVirtualFileSystemOverlaysTask.class)
					.configure(task -> {
						task.parameters(parameters -> {
							parameters.getSources().from(overlays);
							parameters.getDerivedDataPath().set(derivedDataTask.flatMap(it -> it.getParameters().getXcodeDerivedDataPath()));
							parameters.getOutputFile().set(project.getLayout().getBuildDirectory().file(temporaryDirectoryPath(task) + "/all-products-headers.yaml"));
						});
					});

				val targetTask = project.getExtensions().getByType(ModelRegistry.class).register(DomainObjectEntities.newEntity(TaskName.lifecycle(), XcodeTargetExecTask.class, it -> it.ownedBy(entity)))
					.as(XcodeTargetExecTask.class)
					.configure(task -> {
						task.dependsOn(derivedDataTask);
						task.getXcodeProject().set(reference);
						task.getTargetName().set(target.getName());
						task.getOutputs().upToDateWhen(because(String.format("a shell script build phase of %s has no inputs or outputs defined", reference.ofTarget(target.getName())), everyShellScriptBuildPhaseHasDeclaredInputsAndOutputs()));
						task.getDerivedDataPath().set(derivedDataTask.flatMap(it -> it.getParameters().getXcodeDerivedDataPath()));
						task.getOutputDirectory().set(project.getLayout().getBuildDirectory().dir("derivedData/" + task.getName()));
						task.getXcodeInstallation().set(project.getProviders().of(CurrentXcodeInstallationValueSource.class, ActionUtils.doNothing()));
						task.getConfiguration().set(variantInfo.getName());
						task.getBuildSettings().from(ImmutableMap.of("SRCROOT", reference.getLocation().getParent().toString()));
						task.getArguments().add(new CommandLineArgumentProvider() {
							@InputFile
							@PathSensitive(PathSensitivity.ABSOLUTE)
							public Provider<RegularFile> getAllProductsHeaders() {
								return mergeTask.flatMap(it -> it.getParameters().getOutputFile());
							}

							@Override
							public Iterable<String> asArguments() {
								val allProductsHeaders = getAllProductsHeaders().get().getAsFile().getAbsolutePath();
								ImmutableList.Builder<String> builder = ImmutableList.builder();
								builder.add("OTHER_CFLAGS=$(inherited) -ivfsoverlay \"" + allProductsHeaders + "\"");
								builder.add("OTHER_SWIFT_FLAGS=$(inherited) -vfsoverlay \"" + allProductsHeaders + "\"");
								return builder.build();
							}
						});
						action.execute(task);
					});
				entity.addComponent(new XCTargetTaskComponent(ModelNodes.of(targetTask)));

				project.getExtensions().getByType(ModelRegistry.class).register(DomainObjectEntities.newEntity("DerivedDataElements", ConsumableDependencyBucketSpec.class, it -> it.ownedBy(entity)))
					.as(Configuration.class)
					.configure(configuration -> {
						configuration.attributes(attributes -> {
							attributes.attribute(Usage.USAGE_ATTRIBUTE, project.getObjects().named(Usage.class, "xcode-derived-data"));
							attributes.attribute(Attribute.of("dev.nokee.xcode.configuration", String.class), variantInfo.getName());
						});
					})
					.configure(configuration -> {
						configuration.outgoing(outgoing -> {
							outgoing.capability("net.nokeedev.xcode:" + project.getName() + "-" + target.getName() + ":1.0");
							outgoing.artifact(targetTask.flatMap(XcodeTargetExecTask::getOutputDirectory));
						});
					});

				val generateVirtualSystemOverlaysTask = project.getExtensions().getByType(ModelRegistry.class).register(DomainObjectEntities.newEntity(TaskName.of("generate", "virtualFileSystemOverlays"), GenerateVirtualFileSystemOverlaysTask.class, it -> it.ownedBy(entity)))
					.as(GenerateVirtualFileSystemOverlaysTask.class)
					.configure(task -> {
						task.parameters(parameters -> {
							parameters.overlays(new VFSOverlayAction(project.getObjects(), targetTask.asProvider()));
							parameters.getOutputFile().set(project.getLayout().getBuildDirectory().file(temporaryDirectoryPath(task) + "/" + xcTarget.get().getName() + ".yaml"));
						});
					});

				project.getExtensions().getByType(ModelRegistry.class).register(DomainObjectEntities.newEntity("VirtualFileSystemOverlaysElements", ConsumableDependencyBucketSpec.class, it -> it.ownedBy(entity)))
					.as(Configuration.class)
					.configure(configuration -> {
						configuration.attributes(attributes -> {
							attributes.attribute(Usage.USAGE_ATTRIBUTE, project.getObjects().named(Usage.class, "xcode-overlays"));
							attributes.attribute(Attribute.of("dev.nokee.xcode.configuration", String.class), variantInfo.getName());
						});
					})
					.configure(configuration -> {
						configuration.outgoing(outgoing -> {
							outgoing.capability("net.nokeedev.xcode:" + project.getName() + "-" + target.getName() + ":1.0");
							outgoing.artifact(generateVirtualSystemOverlaysTask.flatMap(it -> it.getParameters().getOutputFile()));
						});
					});
			})));

			val projectEntity = project.getExtensions().getByType(ModelLookup.class).get(ModelPath.root());
			projectEntity.addComponent(new XCProjectComponent(reference));
			projectEntity.addComponent(tag(GradleProjectTag.class));
		};
	}

	public static Spec<HasXcodeTargetReference> everyShellScriptBuildPhaseHasDeclaredInputsAndOutputs() {
		return new XcodeTargetExecTaskOutOfDateSpec(XCLoaders.pbxtargetLoader(), new XcodeTargetExecTaskOutOfDateSpec.DefaultPredicate());
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
}
