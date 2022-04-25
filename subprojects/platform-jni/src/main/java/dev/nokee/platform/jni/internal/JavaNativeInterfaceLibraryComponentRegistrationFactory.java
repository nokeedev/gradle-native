/*
 * Copyright 2021 the original author or authors.
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
package dev.nokee.platform.jni.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;
import dev.nokee.language.base.internal.LanguageSourceSetIdentifier;
import dev.nokee.language.jvm.JavaSourceSet;
import dev.nokee.language.jvm.internal.GroovySourceSetRegistrationFactory;
import dev.nokee.language.jvm.internal.JavaSourceSetRegistrationFactory;
import dev.nokee.language.jvm.internal.KotlinSourceSetRegistrationFactory;
import dev.nokee.language.nativebase.internal.NativeLanguagePlugin;
import dev.nokee.language.nativebase.internal.ToolChainSelectorInternal;
import dev.nokee.language.nativebase.tasks.internal.NativeSourceCompileTask;
import dev.nokee.model.DependencyFactory;
import dev.nokee.model.NamedDomainObjectRegistry;
import dev.nokee.model.internal.DomainObjectIdentifierUtils;
import dev.nokee.model.internal.ModelPropertyIdentifier;
import dev.nokee.model.internal.actions.ConfigurableTag;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelComponentReference;
import dev.nokee.model.internal.core.ModelElements;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodeUtils;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.core.ModelPathComponent;
import dev.nokee.model.internal.core.ModelProperties;
import dev.nokee.model.internal.core.ModelProperty;
import dev.nokee.model.internal.core.ModelPropertyRegistrationFactory;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.names.ExcludeFromQualifyingNameTag;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.platform.base.BuildVariant;
import dev.nokee.platform.base.VariantView;
import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.platform.base.internal.CompileTaskTag;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.base.internal.ConfigurationNamer;
import dev.nokee.platform.base.internal.DimensionPropertyRegistrationFactory;
import dev.nokee.platform.base.internal.GroupId;
import dev.nokee.platform.base.internal.IsComponent;
import dev.nokee.platform.base.internal.TaskRegistrationFactory;
import dev.nokee.platform.base.internal.VariantIdentifier;
import dev.nokee.platform.base.internal.Variants;
import dev.nokee.platform.base.internal.dependencies.ConsumableDependencyBucketRegistrationFactory;
import dev.nokee.platform.base.internal.dependencies.DeclarableDependencyBucketRegistrationFactory;
import dev.nokee.platform.base.internal.dependencies.DefaultDependencyBucketFactory;
import dev.nokee.platform.base.internal.dependencies.DependencyBucketIdentifier;
import dev.nokee.platform.base.internal.dependencybuckets.CompileOnlyConfigurationComponent;
import dev.nokee.platform.base.internal.dependencybuckets.ImplementationConfigurationComponent;
import dev.nokee.platform.base.internal.dependencybuckets.LinkOnlyConfigurationComponent;
import dev.nokee.platform.base.internal.dependencybuckets.RuntimeOnlyConfigurationComponent;
import dev.nokee.platform.base.internal.tasks.TaskIdentifier;
import dev.nokee.platform.base.internal.tasks.TaskName;
import dev.nokee.platform.jni.JavaNativeInterfaceLibrary;
import dev.nokee.platform.jni.JniJarBinary;
import dev.nokee.platform.jni.JniLibrary;
import dev.nokee.platform.nativebase.internal.dependencies.FrameworkAwareDependencyBucketFactory;
import dev.nokee.platform.nativebase.internal.rules.BuildableDevelopmentVariantConvention;
import dev.nokee.runtime.core.Coordinate;
import dev.nokee.runtime.nativebase.BinaryLinkage;
import dev.nokee.runtime.nativebase.TargetLinkage;
import dev.nokee.runtime.nativebase.TargetMachine;
import dev.nokee.runtime.nativebase.internal.TargetLinkages;
import dev.nokee.runtime.nativebase.internal.TargetMachines;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.PublishArtifact;
import org.gradle.api.attributes.LibraryElements;
import org.gradle.api.attributes.Usage;
import org.gradle.api.internal.artifacts.dsl.LazyPublishArtifact;
import org.gradle.api.provider.Provider;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import static dev.nokee.language.nativebase.internal.NativePlatformFactory.platformNameFor;
import static dev.nokee.model.internal.actions.ModelAction.configure;
import static dev.nokee.model.internal.actions.ModelAction.configureEach;
import static dev.nokee.model.internal.actions.ModelSpec.descendantOf;
import static dev.nokee.model.internal.actions.ModelSpec.isEqual;
import static dev.nokee.model.internal.core.ModelProjections.createdUsing;
import static dev.nokee.model.internal.type.GradlePropertyTypes.property;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.platform.base.internal.dependencies.DependencyBucketIdentity.consumable;
import static dev.nokee.platform.base.internal.dependencies.DependencyBucketIdentity.declarable;
import static dev.nokee.platform.base.internal.util.PropertyUtils.from;
import static dev.nokee.platform.jni.internal.plugins.JvmIncludeRoots.jvmIncludes;
import static dev.nokee.platform.jni.internal.plugins.NativeCompileTaskProperties.includeRoots;
import static dev.nokee.runtime.nativebase.TargetMachine.TARGET_MACHINE_COORDINATE_AXIS;
import static dev.nokee.utils.ConfigurationUtils.configureAttributes;
import static dev.nokee.utils.ConfigurationUtils.configureExtendsFrom;
import static dev.nokee.utils.TaskUtils.configureBuildGroup;
import static dev.nokee.utils.TaskUtils.configureDependsOn;
import static dev.nokee.utils.TaskUtils.configureDescription;
import static dev.nokee.utils.TransformerUtils.transformEach;
import static java.util.stream.Collectors.joining;
import static org.gradle.language.base.plugins.LifecycleBasePlugin.ASSEMBLE_TASK_NAME;

public final class JavaNativeInterfaceLibraryComponentRegistrationFactory {
	private final Project project;
	private final JavaNativeInterfaceLibraryVariantRegistrationFactory variantFactory;

	public JavaNativeInterfaceLibraryComponentRegistrationFactory(Project project) {
		this.project = project;
		this.variantFactory = new JavaNativeInterfaceLibraryVariantRegistrationFactory(project);
	}

	public ModelRegistration create(ComponentIdentifier identifier) {
		val entityPath = DomainObjectIdentifierUtils.toPath(identifier);
		val builder = ModelRegistration.builder()
			.withComponent(new IdentifierComponent(identifier))
			.withComponent(IsComponent.tag())
			.withComponent(ConfigurableTag.tag())
			.withComponent(createdUsing(of(JniLibraryComponentInternal.class), () -> project.getObjects().newInstance(JniLibraryComponentInternal.class, identifier, GroupId.of(project::getGroup), project.getObjects())))
			.action(ModelActionWithInputs.of(ModelComponentReference.of(ModelPathComponent.class), ModelComponentReference.of(ModelState.class), new ModelActionWithInputs.A2<ModelPathComponent, ModelState>() {
				private boolean alreadyExecuted = false;

				@Override
				@SuppressWarnings({"unchecked", "rawtypes"})
				public void execute(ModelNode entity, ModelPathComponent path, ModelState state) {
					if (entityPath.equals(path.get()) && state.equals(ModelState.Registered) && !alreadyExecuted) {
						alreadyExecuted = true;
						val registry = project.getExtensions().getByType(ModelRegistry.class);

						val bucketFactory = new DeclarableDependencyBucketRegistrationFactory(NamedDomainObjectRegistry.of(project.getConfigurations()), new FrameworkAwareDependencyBucketFactory(project.getObjects(), new DefaultDependencyBucketFactory(NamedDomainObjectRegistry.of(project.getConfigurations()), DependencyFactory.forProject(project))));
						val api = registry.register(bucketFactory.create(DependencyBucketIdentifier.of(declarable("api"), identifier)));
						val implementation = registry.register(bucketFactory.create(DependencyBucketIdentifier.of(declarable("jvmImplementation"), identifier)));
						val runtimeOnly = registry.register(bucketFactory.create(DependencyBucketIdentifier.of(declarable("jvmRuntimeOnly"), identifier)));
						project.getPlugins().withType(NativeLanguagePlugin.class, new Action<NativeLanguagePlugin>() {
							private boolean alreadyExecuted = false;

							@Override
							public void execute(NativeLanguagePlugin appliedPlugin) {
								if (!alreadyExecuted) {
									alreadyExecuted = true;
									val nativeCompileOnly = registry.register(bucketFactory.create(DependencyBucketIdentifier.of(declarable("nativeCompileOnly"), identifier)));
									entity.addComponent(new CompileOnlyConfigurationComponent(ModelNodes.of(nativeCompileOnly)));
								}
							}
						});
						val nativeImplementation = registry.register(bucketFactory.create(DependencyBucketIdentifier.of(declarable("nativeImplementation"), identifier)));
						val nativeLinkOnly = registry.register(bucketFactory.create(DependencyBucketIdentifier.of(declarable("nativeLinkOnly"), identifier)));
						val nativeRuntimeOnly = registry.register(bucketFactory.create(DependencyBucketIdentifier.of(declarable("nativeRuntimeOnly"), identifier)));
						implementation.configure(Configuration.class, configureExtendsFrom(api.as(Configuration.class)));

						entity.addComponent(new ImplementationConfigurationComponent(ModelNodes.of(nativeImplementation)));
						entity.addComponent(new RuntimeOnlyConfigurationComponent(ModelNodes.of(nativeRuntimeOnly)));
						entity.addComponent(new LinkOnlyConfigurationComponent(ModelNodes.of(nativeLinkOnly)));

						val consumableFactory = project.getExtensions().getByType(ConsumableDependencyBucketRegistrationFactory.class);
						val apiElements = registry.register(consumableFactory.create(DependencyBucketIdentifier.of(consumable("apiElements"), identifier)));
						apiElements.configure(Configuration.class, configureAttributes(builder -> builder.usage(project.getObjects().named(Usage.class, Usage.JAVA_API)).attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, project.getObjects().named(LibraryElements.class, LibraryElements.JAR))));
						apiElements.configure(Configuration.class, configureExtendsFrom(api.as(Configuration.class)));
						entity.addComponent(new ApiElementsConfiguration(ModelNodes.of(apiElements)));
						val runtimeElements = registry.register(consumableFactory.create(DependencyBucketIdentifier.of(consumable("runtimeElements"), identifier)));
						runtimeElements.configure(Configuration.class, configureAttributes(builder -> builder.usage(project.getObjects().named(Usage.class, Usage.JAVA_RUNTIME)).attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, project.getObjects().named(LibraryElements.class, LibraryElements.JAR))));
						runtimeElements.configure(Configuration.class, configureExtendsFrom(api.as(Configuration.class)));
						entity.addComponent(new RuntimeElementsConfiguration(ModelNodes.of(runtimeElements)));

						val variants = ModelElements.of(entity).property("variants").as(of(VariantView.class));

						val developmentVariantProperty = registry.register(project.getExtensions().getByType(ModelPropertyRegistrationFactory.class).createProperty(ModelPropertyIdentifier.of(identifier, "developmentVariant"), JniLibrary.class));
						((ModelProperty<JniLibrary>) developmentVariantProperty).asProperty(property(of(JniLibrary.class))).convention(project.provider(new BuildableDevelopmentVariantConvention(variants.as(VariantView.class).flatMap(VariantView::getElements)::get)));

						val assembleTask = registry.register(project.getExtensions().getByType(TaskRegistrationFactory.class).create(TaskIdentifier.of(TaskName.of(ASSEMBLE_TASK_NAME), identifier), Task.class).build());
						assembleTask.configure(Task.class, configureBuildGroup());
						assembleTask.configure(Task.class, configureDescription("Assembles the outputs of %s.", identifier));
						entity.addComponent(new AssembleTask(ModelNodes.of(assembleTask)));

						// TODO: This is an external dependency meaning we should go through the component dependencies.
						//  We can either add an file dependency or use the, yet-to-be-implemented, shim to consume system libraries
						//  We aren't using a language source set as the files will be included inside the IDE projects which is not what we want.
						registry.instantiate(configureEach(descendantOf(entity.getId()), NativeSourceCompileTask.class, includeRoots(from(jvmIncludes()))));

						val dimensions = project.getExtensions().getByType(DimensionPropertyRegistrationFactory.class);
						val toolChainSelectorInternal = project.getObjects().newInstance(ToolChainSelectorInternal.class);
						registry.register(dimensions.newAxisProperty(ModelPropertyIdentifier.of(identifier, "targetMachines"))
							.axis(TARGET_MACHINE_COORDINATE_AXIS)
							.defaultValue(TargetMachines.host())
							.validateUsing((Iterable<Coordinate<TargetMachine>> it) -> assertTargetMachinesAreKnown(it, toolChainSelectorInternal))
							.build());
						registry.register(dimensions.newAxisProperty(ModelPropertyIdentifier.of(identifier, "targetLinkages"))
							.elementType(TargetLinkage.class)
							.axis(BinaryLinkage.BINARY_LINKAGE_COORDINATE_AXIS)
							.defaultValue(TargetLinkages.SHARED)
							.build());

						project.getPluginManager().withPlugin("groovy", ignored -> {
							registry.register(project.getExtensions().getByType(GroovySourceSetRegistrationFactory.class).create(LanguageSourceSetIdentifier.of(identifier, "groovy")));
						});
						project.getPluginManager().withPlugin("java", ignored -> {
							val sourceSetIdentifier = LanguageSourceSetIdentifier.of(identifier, "java");
							val sourceSet = registry.register(project.getExtensions().getByType(JavaSourceSetRegistrationFactory.class).create(sourceSetIdentifier));

							sourceSet.configure(JavaSourceSet.class, it -> {
								it.getCompileTask().configure(new ConfigureJniHeaderDirectoryOnJavaCompileAction(sourceSetIdentifier, project.getLayout()));
							});

							entity.addComponent(new GeneratedJniHeadersComponent(project.getObjects().fileCollection().from((Callable<?>) () -> {
								return sourceSet.as(JavaSourceSet.class).flatMap(ss -> ss.getCompileTask().flatMap(it -> it.getOptions().getHeaderOutputDirectory()));
							})));
							entity.addComponent(new JavaLanguageSourceSet(sourceSet));
						});
						project.getPluginManager().withPlugin("org.jetbrains.kotlin.jvm", ignored -> {
							registry.register(project.getExtensions().getByType(KotlinSourceSetRegistrationFactory.class).create(LanguageSourceSetIdentifier.of(identifier, "kotlin")));
						});

						project.getPluginManager().withPlugin("java", appliedPlugin -> {
							project.getConfigurations().named(ConfigurationNamer.INSTANCE.determineName(DependencyBucketIdentifier.of(declarable("implementation"), identifier)), configureExtendsFrom(implementation.as(Configuration.class)));
							project.getConfigurations().named(ConfigurationNamer.INSTANCE.determineName(DependencyBucketIdentifier.of(declarable("runtimeOnly"), identifier)), configureExtendsFrom(runtimeOnly.as(Configuration.class)));
						});
					}
				}
			}))
			.action(ModelActionWithInputs.of(ModelComponentReference.of(IdentifierComponent.class), ModelComponentReference.of(JvmJarArtifactComponent.class), ModelComponentReference.of(ApiElementsConfiguration.class), (entity, id, jvmJar, apiElements) -> {
				if (id.get().equals(identifier)) {
					val registry = project.getExtensions().getByType(ModelRegistry.class);
					registry.instantiate(configure(apiElements.get().getId(), Configuration.class, configuration -> {
						configuration.getOutgoing().artifact(jvmJar.getJarTask());
					}));
				}
			}))
			.action(ModelActionWithInputs.of(ModelComponentReference.of(IdentifierComponent.class), ModelComponentReference.of(JvmJarArtifactComponent.class), ModelComponentReference.of(RuntimeElementsConfiguration.class), (entity, id, jvmJar, runtimeElements) -> {
				if (id.get().equals(identifier)) {
					val registry = project.getExtensions().getByType(ModelRegistry.class);
					registry.instantiate(configure(runtimeElements.get().getId(), Configuration.class, configuration -> {
						configuration.getOutgoing().artifact(jvmJar.getJarTask());
					}));
				}
			}))
			.action(ModelActionWithInputs.of(ModelComponentReference.of(IdentifierComponent.class), ModelComponentReference.of(AssembleTask.class), ModelComponentReference.ofProjection(JavaNativeInterfaceLibrary.class), (entity, id, assemble, projection) -> {
				if (id.get().equals(identifier)) {
					val component = project.provider(() -> projection.get(of(JavaNativeInterfaceLibrary.class)));
					Provider<List<JniLibrary>> allBuildableVariants = component.flatMap(it -> it.getVariants().filter(v -> v.getSharedLibrary().isBuildable()));
					assemble.configure(configureDependsOn(component.flatMap(JavaNativeInterfaceLibrary::getDevelopmentVariant).map(JniLibrary::getJavaNativeInterfaceJar).map(Collections::singletonList).orElse(Collections.emptyList())));
					assemble.configure(task -> {
						task.dependsOn((Callable<Object>) () -> {
							val buildVariants = component.get().getBuildVariants().get();
							val firstBuildVariant = Iterables.getFirst(buildVariants, null);
							if (buildVariants.size() == 1 && allBuildableVariants.get().isEmpty() && firstBuildVariant.hasAxisOf(TargetMachines.host().getOperatingSystemFamily())) {
								throw new RuntimeException(String.format("No tool chain is available to build for platform '%s'", platformNameFor(((BuildVariantInternal) firstBuildVariant).getAxisValue(TARGET_MACHINE_COORDINATE_AXIS))));
							}
							return ImmutableList.of();
						});
					});
				}
			}))
			.action(ModelActionWithInputs.of(ModelComponentReference.of(IdentifierComponent.class), ModelComponentReference.of(RuntimeElementsConfiguration.class), ModelComponentReference.ofProjection(JavaNativeInterfaceLibrary.class), (entity, id, runtimeElements, projection) -> {
				if (id.get().equals(identifier)) {
					val component = project.provider(() -> projection.get(of(JavaNativeInterfaceLibrary.class)));
					val toolChainSelector = project.getObjects().newInstance(ToolChainSelectorInternal.class);
					val values = project.getObjects().listProperty(PublishArtifact.class);
					Provider<List<JniLibrary>> allBuildableVariants = component.flatMap(it -> it.getVariants().filter(v -> toolChainSelector.canBuild(v.getTargetMachine())));
					Provider<Iterable<JniJarBinary>> allJniJars = allBuildableVariants.map(transformEach(v -> v.getJavaNativeInterfaceJar()));
					val allArtifacts = project.getObjects().listProperty(PublishArtifact.class);
					allArtifacts.set(allJniJars.flatMap(binaries -> {
						val result = project.getObjects().listProperty(PublishArtifact.class);
						for (JniJarBinary binary : binaries) {
							result.add(new LazyPublishArtifact(binary.getJarTask()));
						}
						return result;
					}));
					allArtifacts.finalizeValueOnRead();
					values.addAll(allArtifacts);
					runtimeElements.addAll(values);
				}
			}))
			.action(ModelActionWithInputs.of(ModelComponentReference.of(IdentifierComponent.class), ModelComponentReference.of(ModelState.IsAtLeastFinalized.class), (entity, id, ignored) -> {
				if (id.get().equals(identifier)) {
					val component = ModelNodeUtils.get(entity, JniLibraryComponentInternal.class);

					val variants = ImmutableMap.<BuildVariant, ModelNode>builder();
					component.getBuildVariants().get().forEach(buildVariant -> {
						val variantIdentifier = VariantIdentifier.builder().withBuildVariant((BuildVariantInternal) buildVariant).withComponentIdentifier(component.getIdentifier()).withType(JniLibraryInternal.class).build();
						val variant = project.getExtensions().getByType(ModelRegistry.class).register(variantFactory.create(variantIdentifier));
						variant.configure(JniLibrary.class, it -> it.getBaseName().convention(ModelProperties.getProperty(entity, "baseName").as(String.class).asProvider()));

						// See https://github.com/nokeedev/gradle-native/issues/543
						if (component.getBuildVariants().get().size() > 1) {
							variant.configure(JniLibrary.class, it -> {
								it.getJavaNativeInterfaceJar().getJarTask().configure(task -> {
									task.getDestinationDirectory().set(project.getLayout().getBuildDirectory().dir("libs/" + identifier.getName()));
								});
							});
						}

						variants.put(buildVariant, ModelNodes.of(variant));
					});
					entity.addComponent(new Variants(variants.build()));
				}
			}))
			;

		if (identifier.isMainComponent()) {
			builder.withComponent(ExcludeFromQualifyingNameTag.tag());
		}

		return builder.build();
	}

	private static void assertTargetMachinesAreKnown(Iterable<Coordinate<TargetMachine>> targetMachines, ToolChainSelectorInternal toolChainSelector) {
		List<TargetMachine> unknownTargetMachines = Streams.stream(targetMachines).filter(it -> !toolChainSelector.isKnown(it.getValue())).map(Coordinate::getValue).collect(Collectors.toList());
		if (!unknownTargetMachines.isEmpty()) {
			throw new IllegalArgumentException("The following target machines are not know by the defined tool chains:\n" + unknownTargetMachines.stream().map(it -> " * " + it.getOperatingSystemFamily().getCanonicalName() + " " + it.getArchitecture().getCanonicalName()).collect(joining("\n")));
		}
	}
}
