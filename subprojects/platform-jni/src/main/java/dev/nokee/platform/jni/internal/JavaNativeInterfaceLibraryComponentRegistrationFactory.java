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
import dev.nokee.language.base.ConfigurableSourceSet;
import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.internal.LanguageSourceSetIdentifier;
import dev.nokee.language.jvm.JavaSourceSet;
import dev.nokee.language.jvm.internal.GroovySourceSetRegistrationFactory;
import dev.nokee.language.jvm.internal.JavaSourceSetRegistrationFactory;
import dev.nokee.language.jvm.internal.KotlinSourceSetRegistrationFactory;
import dev.nokee.language.nativebase.HasHeaders;
import dev.nokee.language.nativebase.internal.NativeLanguagePlugin;
import dev.nokee.language.nativebase.internal.ProjectHeaderSearchPaths;
import dev.nokee.language.nativebase.internal.ToolChainSelectorInternal;
import dev.nokee.language.nativebase.tasks.internal.NativeSourceCompileTask;
import dev.nokee.model.DependencyFactory;
import dev.nokee.model.NamedDomainObjectRegistry;
import dev.nokee.model.internal.DomainObjectIdentifierUtils;
import dev.nokee.model.internal.FullyQualifiedNameComponent;
import dev.nokee.model.internal.ModelPropertyIdentifier;
import dev.nokee.model.internal.core.*;
import dev.nokee.model.internal.registry.ModelConfigurer;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.model.internal.type.TypeOf;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.platform.base.BuildVariant;
import dev.nokee.platform.base.VariantView;
import dev.nokee.platform.base.internal.*;
import dev.nokee.platform.base.internal.dependencies.ConsumableDependencyBucketRegistrationFactory;
import dev.nokee.platform.base.internal.dependencies.DeclarableDependencyBucketRegistrationFactory;
import dev.nokee.platform.base.internal.dependencies.DefaultDependencyBucketFactory;
import dev.nokee.platform.base.internal.dependencies.DependencyBucketIdentifier;
import dev.nokee.platform.base.internal.tasks.TaskIdentifier;
import dev.nokee.platform.base.internal.tasks.TaskName;
import dev.nokee.platform.jni.*;
import dev.nokee.platform.nativebase.SharedLibraryBinary;
import dev.nokee.platform.nativebase.internal.dependencies.FrameworkAwareDependencyBucketFactory;
import dev.nokee.platform.nativebase.internal.rules.BuildableDevelopmentVariantConvention;
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
import org.gradle.api.plugins.AppliedPlugin;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import static dev.nokee.language.nativebase.internal.NativePlatformFactory.platformNameFor;
import static dev.nokee.model.internal.core.ModelActions.once;
import static dev.nokee.model.internal.core.ModelComponentType.projectionOf;
import static dev.nokee.model.internal.core.ModelNodeUtils.applyTo;
import static dev.nokee.model.internal.core.ModelNodes.stateAtLeast;
import static dev.nokee.model.internal.core.ModelProjections.createdUsing;
import static dev.nokee.model.internal.core.NodePredicate.allDescendants;
import static dev.nokee.model.internal.core.NodePredicate.self;
import static dev.nokee.model.internal.type.GradlePropertyTypes.property;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.platform.base.internal.LanguageSourceSetConventionSupplier.maven;
import static dev.nokee.platform.base.internal.LanguageSourceSetConventionSupplier.withConventionOf;
import static dev.nokee.platform.base.internal.dependencies.DependencyBucketIdentity.consumable;
import static dev.nokee.platform.base.internal.dependencies.DependencyBucketIdentity.declarable;
import static dev.nokee.platform.base.internal.util.PropertyUtils.from;
import static dev.nokee.platform.jni.internal.plugins.JvmIncludeRoots.jvmIncludes;
import static dev.nokee.platform.jni.internal.plugins.NativeCompileTaskProperties.includeRoots;
import static dev.nokee.runtime.nativebase.TargetMachine.TARGET_MACHINE_COORDINATE_AXIS;
import static dev.nokee.utils.ConfigurationUtils.configureAttributes;
import static dev.nokee.utils.ConfigurationUtils.configureExtendsFrom;
import static dev.nokee.utils.TaskUtils.*;
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
		return ModelRegistration.builder()
			.withComponent(entityPath)
			.withComponent(identifier)
			.withComponent(new FullyQualifiedNameComponent(ComponentNamer.INSTANCE.determineName(identifier)))
			.action(ModelActionWithInputs.of(ModelComponentReference.of(ModelPath.class), ModelComponentReference.ofAny(projectionOf(LanguageSourceSet.class)), ModelComponentReference.of(ModelState.IsAtLeastRealized.class), (entity, path, projection, ignored) -> {
				if (entityPath.isDirectDescendant(path)) {
					withConventionOf(maven(identifier.getName())).accept(ModelNodeUtils.get(entity, LanguageSourceSet.class));
				}
			}))
			.withComponent(IsComponent.tag())
			.withComponent(createdUsing(of(JniLibraryComponentInternal.class), () -> project.getObjects().newInstance(JniLibraryComponentInternal.class, identifier, GroupId.of(project::getGroup), project.getObjects())))
			.action(ModelActionWithInputs.of(ModelComponentReference.of(ModelPath.class), ModelComponentReference.of(ModelState.class), new ModelActionWithInputs.A2<ModelPath, ModelState>() {
				private boolean alreadyExecuted = false;

				@Override
				@SuppressWarnings({"unchecked", "rawtypes"})
				public void execute(ModelNode entity, ModelPath path, ModelState state) {
					if (entityPath.equals(path) && state.equals(ModelState.Registered) && !alreadyExecuted) {
						alreadyExecuted = true;
						val registry = project.getExtensions().getByType(ModelRegistry.class);

//						registry.register(project.getExtensions().getByType(CHeaderSetRegistrationFactory.class).create(LanguageSourceSetIdentifier.of(identifier, "jni")));

						// TODO: ONLY if applying include language plugin
//						registry.register(project.getExtensions().getByType(CHeaderSetRegistrationFactory.class).create(LanguageSourceSetIdentifier.of(identifier, "headers")));

						val baseNameProperty = registry.register(project.getExtensions().getByType(ModelPropertyRegistrationFactory.class).createProperty(ModelPropertyIdentifier.of(identifier, "baseName"), String.class));
						((ModelProperty<String>) baseNameProperty).asProperty(property(of(String.class))).convention(identifier.getName().get());

						registry.register(project.getExtensions().getByType(ComponentSourcesPropertyRegistrationFactory.class).create(ModelPropertyIdentifier.of(identifier, "sources"), JavaNativeInterfaceLibrarySources.class, JavaNativeInterfaceSourcesViewAdapter::new));

						project.getPluginManager().withPlugin("groovy", ignored -> {
							registry.register(project.getExtensions().getByType(GroovySourceSetRegistrationFactory.class).create(LanguageSourceSetIdentifier.of(identifier, "groovy")));
						});
						project.getPluginManager().withPlugin("java", ignored -> {
							val sourceSetIdentifier = LanguageSourceSetIdentifier.of(identifier, "java");
							val sourceSet = registry.register(project.getExtensions().getByType(JavaSourceSetRegistrationFactory.class).create(sourceSetIdentifier));

							sourceSet.configure(JavaSourceSet.class, it -> {
								it.getCompileTask().configure(new ConfigureJniHeaderDirectoryOnJavaCompileAction(sourceSetIdentifier, project.getLayout()));
							});

							entity.addComponent(new JavaLanguageSourceSet(sourceSet));
						});
						project.getPluginManager().withPlugin("org.jetbrains.kotlin.jvm", ignored -> {
							registry.register(project.getExtensions().getByType(KotlinSourceSetRegistrationFactory.class).create(LanguageSourceSetIdentifier.of(identifier, "kotlin")));
						});

						val bucketFactory = new DeclarableDependencyBucketRegistrationFactory(NamedDomainObjectRegistry.of(project.getConfigurations()), new FrameworkAwareDependencyBucketFactory(project.getObjects(), new DefaultDependencyBucketFactory(NamedDomainObjectRegistry.of(project.getConfigurations()), DependencyFactory.forProject(project))));
						registry.register(project.getExtensions().getByType(ComponentDependenciesPropertyRegistrationFactory.class).create(ModelPropertyIdentifier.of(identifier, "dependencies"), JavaNativeInterfaceLibraryComponentDependencies.class, ModelBackedJavaNativeInterfaceLibraryComponentDependencies::new));
						val api = registry.register(bucketFactory.create(DependencyBucketIdentifier.of(declarable("api"), identifier)));
						val implementation = registry.register(bucketFactory.create(DependencyBucketIdentifier.of(declarable("jvmImplementation"), identifier)));
						val runtimeOnly = registry.register(bucketFactory.create(DependencyBucketIdentifier.of(declarable("jvmRuntimeOnly"), identifier)));
						project.getPluginManager().withPlugin("java", appliedPlugin -> {
							project.getConfigurations().named(ConfigurationNamer.INSTANCE.determineName(DependencyBucketIdentifier.of(declarable("implementation"), identifier)), configureExtendsFrom(implementation.as(Configuration.class)));
							project.getConfigurations().named(ConfigurationNamer.INSTANCE.determineName(DependencyBucketIdentifier.of(declarable("runtimeOnly"), identifier)), configureExtendsFrom(runtimeOnly.as(Configuration.class)));
						});
						project.getPlugins().withType(NativeLanguagePlugin.class, new Action<NativeLanguagePlugin>() {
							private boolean alreadyExecuted = false;

							@Override
							public void execute(NativeLanguagePlugin appliedPlugin) {
								if (!alreadyExecuted) {
									alreadyExecuted = true;
									registry.register(bucketFactory.create(DependencyBucketIdentifier.of(declarable("nativeCompileOnly"), identifier)));
								}
							}
						});
						registry.register(bucketFactory.create(DependencyBucketIdentifier.of(declarable("nativeImplementation"), identifier)));
						registry.register(bucketFactory.create(DependencyBucketIdentifier.of(declarable("nativeLinkOnly"), identifier)));
						registry.register(bucketFactory.create(DependencyBucketIdentifier.of(declarable("nativeRuntimeOnly"), identifier)));
						implementation.configure(Configuration.class, configureExtendsFrom(api.as(Configuration.class)));

						val consumableFactory = project.getExtensions().getByType(ConsumableDependencyBucketRegistrationFactory.class);
						val apiElements = registry.register(consumableFactory.create(DependencyBucketIdentifier.of(consumable("apiElements"), identifier)));
						apiElements.configure(Configuration.class, configureAttributes(builder -> builder.usage(project.getObjects().named(Usage.class, Usage.JAVA_API)).attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, project.getObjects().named(LibraryElements.class, LibraryElements.JAR))));
						apiElements.configure(Configuration.class, configureExtendsFrom(api.as(Configuration.class)));
						entity.addComponent(new ApiElementsConfiguration(ModelNodes.of(apiElements)));
						val runtimeElements = registry.register(consumableFactory.create(DependencyBucketIdentifier.of(consumable("runtimeElements"), identifier)));
						runtimeElements.configure(Configuration.class, configureAttributes(builder -> builder.usage(project.getObjects().named(Usage.class, Usage.JAVA_RUNTIME)).attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, project.getObjects().named(LibraryElements.class, LibraryElements.JAR))));
						runtimeElements.configure(Configuration.class, configureExtendsFrom(api.as(Configuration.class)));
						entity.addComponent(new RuntimeElementsConfiguration(ModelNodes.of(runtimeElements)));

						val variants = registry.register(project.getExtensions().getByType(ComponentVariantsPropertyRegistrationFactory.class).create(ModelPropertyIdentifier.of(identifier, "variants"), JniLibrary.class));

						val developmentVariantProperty = registry.register(project.getExtensions().getByType(ModelPropertyRegistrationFactory.class).createProperty(ModelPropertyIdentifier.of(identifier, "developmentVariant"), JniLibrary.class));
						((ModelProperty<JniLibrary>) developmentVariantProperty).asProperty(property(of(JniLibrary.class))).convention(project.provider(new BuildableDevelopmentVariantConvention(variants.as(VariantView.class).flatMap(VariantView::getElements)::get)));

						registry.register(project.getExtensions().getByType(ComponentTasksPropertyRegistrationFactory.class).create(ModelPropertyIdentifier.of(identifier, "tasks")));

						val assembleTask = registry.register(project.getExtensions().getByType(TaskRegistrationFactory.class).create(TaskIdentifier.of(TaskName.of(ASSEMBLE_TASK_NAME), identifier), Task.class).build());
						assembleTask.configure(Task.class, configureBuildGroup());
						assembleTask.configure(Task.class, configureDescription("Assembles the outputs of %s.", identifier));
						entity.addComponent(new AssembleTask(ModelNodes.of(assembleTask)));

						val registerJvmJarBinaryAction = new Action<AppliedPlugin>() {
							private boolean alreadyExecuted = false;

							@Override
							public void execute(AppliedPlugin ignored) {
								if (!alreadyExecuted) {
									alreadyExecuted = true;
									val jvmJar = registry.register(project.getExtensions().getByType(JvmJarBinaryRegistrationFactory.class).create(BinaryIdentifier.of(identifier, BinaryIdentity.ofMain("jvmJar", "JVM JAR binary"))));
									jvmJar.configure(JvmJarBinary.class, binary -> {
										binary.getJarTask().configure(task -> task.getArchiveBaseName().set(baseNameProperty.as(String.class).asProvider()));
									});
									entity.addComponent(new JvmJarArtifact(ModelNodes.of(jvmJar)));
								}
							}
						};
						project.getPluginManager().withPlugin("java", registerJvmJarBinaryAction);
						project.getPluginManager().withPlugin("groovy", registerJvmJarBinaryAction);
						project.getPluginManager().withPlugin("org.jetbrains.kotlin.jvm", registerJvmJarBinaryAction);

						val binaries = registry.register(project.getExtensions().getByType(ComponentBinariesPropertyRegistrationFactory.class).create(ModelPropertyIdentifier.of(identifier, "binaries")));

						// TODO: This is an external dependency meaning we should go through the component dependencies.
						//  We can either add an file dependency or use the, yet-to-be-implemented, shim to consume system libraries
						//  We aren't using a language source set as the files will be included inside the IDE projects which is not what we want.
						binaries.configure(of(new TypeOf<BinaryView<Binary>>() {}), binaryView -> {
							binaryView.configureEach(SharedLibraryBinary.class, binary -> {
								binary.getCompileTasks().configureEach(NativeSourceCompileTask.class, includeRoots(from(jvmIncludes())));
							});
						});

						val dimensions = project.getExtensions().getByType(DimensionPropertyRegistrationFactory.class);
						val buildVariants = entity.addComponent(new BuildVariants(entity, project.getProviders(), project.getObjects()));
						entity.addComponent(new ModelBackedVariantDimensions(identifier, registry, dimensions));
						val toolChainSelectorInternal = project.getObjects().newInstance(ToolChainSelectorInternal.class);
						registry.register(dimensions.newAxisProperty(ModelPropertyIdentifier.of(identifier, "targetMachines"))
							.axis(TARGET_MACHINE_COORDINATE_AXIS)
							.defaultValue(TargetMachines.host())
							.validateUsing((Iterable<TargetMachine> it) -> assertTargetMachinesAreKnown(it, toolChainSelectorInternal))
							.build());
						registry.register(dimensions.newAxisProperty(ModelPropertyIdentifier.of(identifier, "targetLinkages"))
							.elementType(TargetLinkage.class)
							.axis(BinaryLinkage.BINARY_LINKAGE_COORDINATE_AXIS)
							.defaultValue(TargetLinkages.SHARED)
							.build());
						registry.register(dimensions.buildVariants(ModelPropertyIdentifier.of(identifier, "buildVariants"), buildVariants.get()));
					}
				}
			}))
			.action(ModelActionWithInputs.of(ModelComponentReference.of(ComponentIdentifier.class), ModelComponentReference.of(JvmJarArtifact.class), ModelComponentReference.of(ApiElementsConfiguration.class), (entity, id, jvmJar, apiElements) -> {
				if (id.equals(identifier)) {
					apiElements.add(jvmJar.getJarTask());
				}
			}))
			.action(ModelActionWithInputs.of(ModelComponentReference.of(ComponentIdentifier.class), ModelComponentReference.of(JvmJarArtifact.class), ModelComponentReference.of(RuntimeElementsConfiguration.class), (entity, id, jvmJar, runtimeElements) -> {
				if (id.equals(identifier)) {
					runtimeElements.add(jvmJar.getJarTask());
				}
			}))
			.action(ModelActionWithInputs.of(ModelComponentReference.of(ComponentIdentifier.class), ModelComponentReference.of(JvmJarArtifact.class), ModelComponentReference.of(AssembleTask.class), (entity, id, jvmJar, assemble) -> {
				if (id.equals(identifier)) {
					assemble.configure(configureDependsOn(jvmJar));
				}
			}))
			.action(ModelActionWithInputs.of(ModelComponentReference.of(ComponentIdentifier.class), ModelComponentReference.of(AssembleTask.class), ModelComponentReference.ofProjection(JavaNativeInterfaceLibrary.class).asProvider(), (entity, id, assemble, component) -> {
				if (id.equals(identifier)) {
					Provider<List<JniLibrary>> allBuildableVariants = component.flatMap(it -> it.getVariants().filter(v -> v.getSharedLibrary().isBuildable()));
					Provider<Iterable<JniJarBinary>> allJniJars = allBuildableVariants.map(transformEach(v -> v.getJavaNativeInterfaceJar()));
					assemble.configure(configureDependsOn(allJniJars));
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
			.action(ModelActionWithInputs.of(ModelComponentReference.of(ComponentIdentifier.class), ModelComponentReference.of(RuntimeElementsConfiguration.class), ModelComponentReference.ofProjection(JavaNativeInterfaceLibrary.class).asProvider(), (entity, id, runtimeElements, component) -> {
				if (id.equals(identifier)) {
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
			.action(ModelActionWithInputs.of(ModelComponentReference.of(ComponentIdentifier.class), ModelComponentReference.of(ModelState.IsAtLeastFinalized.class), (entity, id, ignored) -> {
				if (id.equals(identifier)) {
					val component = ModelNodeUtils.get(entity, JniLibraryComponentInternal.class);

					val variants = ImmutableMap.<BuildVariant, ModelNode>builder();
					component.getBuildVariants().get().forEach(buildVariant -> {
						val variantIdentifier = VariantIdentifier.builder().withBuildVariant((BuildVariantInternal) buildVariant).withComponentIdentifier(component.getIdentifier()).withType(JniLibrary.class).build();
						val variant = project.getExtensions().getByType(ModelRegistry.class).register(variantFactory.create(variantIdentifier));
						variant.configure(JniLibrary.class, it -> it.getBaseName().convention(ModelProperties.getProperty(entity, "baseName").as(String.class).asProvider()));

						variants.put(buildVariant, ModelNodes.of(variant));
					});
					entity.addComponent(new Variants(variants.build()));
				}
			}))
			.action(ModelActionWithInputs.of(ModelComponentReference.of(ComponentIdentifier.class), ModelComponentReference.of(Variants.class), ModelComponentReference.of(JavaLanguageSourceSet.class), (entity, id, variants, sourceSet) -> {
				if (id.equals(identifier)) {
					whenElementKnown(entity, ModelActionWithInputs.of(ModelComponentReference.of(LanguageSourceSetIdentifier.class), ModelComponentReference.ofProjection(LanguageSourceSet.class).asDomainObject(), ModelComponentReference.of(ProjectHeaderSearchPaths.class), ModelComponentReference.of(ModelState.IsAtLeastRealized.class), (e, i, ss, l, ignored) -> {
						if (!i.getOwnerIdentifier().equals(identifier) && ss instanceof HasHeaders) {
							((ConfigurableSourceSet) ((HasHeaders) ss).getHeaders()).convention("src/" + identifier.getName() + "/headers", sourceSet.as(JavaSourceSet.class).flatMap(JavaSourceSet::getCompileTask).flatMap(it -> it.getOptions().getHeaderOutputDirectory()));
						}
					}));
				}
			}))
			.action(ModelActionWithInputs.of(ModelComponentReference.of(ComponentIdentifier.class), ModelComponentReference.of(Variants.class), ModelComponentReference.of(JvmJarArtifact.class), (entity, id, variants, jvmJar) -> {
				if (id.equals(identifier)) {
					variants.forEach(it -> {
						applyTo(it, self(ModelActionWithInputs.of(ModelComponentReference.of(AssembleTask.class), (e, assembleTask) -> {
							assembleTask.configure(configureDependsOn(jvmJar));
						})));
					});
				}
			}))
			.action(ModelActionWithInputs.of(ModelComponentReference.of(ComponentIdentifier.class), ModelComponentReference.of(ModelState.IsAtLeastFinalized.class), ModelComponentReference.of(JvmJarArtifact.class), ModelComponentReference.ofProjection(JavaNativeInterfaceLibrary.class).asDomainObject(), (entity, id, ignored, jvmJar, component) -> {
				if (id.equals(identifier)) {
					if (component.getBuildVariants().get().size() == 1) {
						project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelComponentReference.of(BinaryIdentifier.class), ModelComponentReference.ofProjection(JvmJarBinary.class).asDomainObject(), (e, i, binary) -> {
							if (i.getOwnerIdentifier().equals(identifier)) {
								binary.getJarTask().configure(configureDescription("Assembles a JAR archive containing the classes and shared library for %s.", ModelNodes.of(binary).getComponent(BinaryIdentifier.class)));
							}
						}));
					}
				}
			}))
			.build()
			;
	}

	private static void whenElementKnown(Object target, ModelAction action) {
		applyTo(ModelNodes.of(target), allDescendants(stateAtLeast(ModelState.Created)).apply(once(action)));
	}

	private static void assertTargetMachinesAreKnown(Iterable<TargetMachine> targetMachines, ToolChainSelectorInternal toolChainSelector) {
		List<TargetMachine> unknownTargetMachines = Streams.stream(targetMachines).filter(it -> !toolChainSelector.isKnown(it)).collect(Collectors.toList());
		if (!unknownTargetMachines.isEmpty()) {
			throw new IllegalArgumentException("The following target machines are not know by the defined tool chains:\n" + unknownTargetMachines.stream().map(it -> " * " + it.getOperatingSystemFamily().getCanonicalName() + " " + it.getArchitecture().getCanonicalName()).collect(joining("\n")));
		}
	}
}
