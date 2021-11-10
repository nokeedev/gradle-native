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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Streams;
import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.internal.LanguageSourceSetIdentifier;
import dev.nokee.language.c.internal.plugins.CHeaderSetRegistrationFactory;
import dev.nokee.language.jvm.internal.GroovySourceSetRegistrationFactory;
import dev.nokee.language.jvm.internal.JavaSourceSetRegistrationFactory;
import dev.nokee.language.jvm.internal.KotlinSourceSetRegistrationFactory;
import dev.nokee.language.nativebase.internal.NativeLanguagePlugin;
import dev.nokee.language.nativebase.internal.ToolChainSelectorInternal;
import dev.nokee.language.objectivec.ObjectiveCSourceSet;
import dev.nokee.language.objectivecpp.ObjectiveCppSourceSet;
import dev.nokee.model.DependencyFactory;
import dev.nokee.model.NamedDomainObjectRegistry;
import dev.nokee.model.internal.DomainObjectIdentifierUtils;
import dev.nokee.model.internal.ModelPropertyIdentifier;
import dev.nokee.model.internal.core.*;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.model.internal.type.ModelType;
import dev.nokee.platform.base.BuildVariant;
import dev.nokee.platform.base.internal.*;
import dev.nokee.platform.base.internal.dependencies.ConsumableDependencyBucketRegistrationFactory;
import dev.nokee.platform.base.internal.dependencies.DeclarableDependencyBucketRegistrationFactory;
import dev.nokee.platform.base.internal.dependencies.DefaultDependencyBucketFactory;
import dev.nokee.platform.base.internal.dependencies.DependencyBucketIdentifier;
import dev.nokee.platform.base.internal.tasks.TaskIdentifier;
import dev.nokee.platform.base.internal.tasks.TaskName;
import dev.nokee.platform.base.internal.tasks.TaskRegistry;
import dev.nokee.platform.jni.JavaNativeInterfaceLibrary;
import dev.nokee.platform.jni.JavaNativeInterfaceLibraryComponentDependencies;
import dev.nokee.platform.jni.JavaNativeInterfaceLibrarySources;
import dev.nokee.platform.jni.JniLibrary;
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
import org.gradle.api.attributes.LibraryElements;
import org.gradle.api.attributes.Usage;
import org.gradle.api.plugins.AppliedPlugin;

import java.util.List;
import java.util.stream.Collectors;

import static dev.nokee.model.internal.core.ModelComponentType.projectionOf;
import static dev.nokee.model.internal.core.ModelProjections.createdUsing;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.platform.base.internal.LanguageSourceSetConventionSupplier.*;
import static dev.nokee.platform.base.internal.dependencies.DependencyBucketIdentity.consumable;
import static dev.nokee.platform.base.internal.dependencies.DependencyBucketIdentity.declarable;
import static dev.nokee.runtime.nativebase.TargetMachine.TARGET_MACHINE_COORDINATE_AXIS;
import static dev.nokee.utils.ConfigurationUtils.configureAttributes;
import static dev.nokee.utils.ConfigurationUtils.configureExtendsFrom;
import static dev.nokee.utils.TaskUtils.configureBuildGroup;
import static dev.nokee.utils.TaskUtils.configureDescription;
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
			.withComponent(new FullyQualifiedName(ComponentNamer.INSTANCE.determineName(identifier)))
			.withComponent(createdUsing(of(JavaNativeInterfaceLibrary.class), () -> project.getObjects().newInstance(DefaultJavaNativeInterfaceLibrary.class)))
			.action(ModelActionWithInputs.of(ModelComponentReference.of(ModelPath.class), ModelComponentReference.ofAny(projectionOf(LanguageSourceSet.class)), ModelComponentReference.of(ModelState.IsAtLeastRealized.class), (entity, path, projection, ignored) -> {
				if (entityPath.isDescendant(path)) {
					withConventionOf(maven(identifier.getName())).accept(ModelNodeUtils.get(entity, LanguageSourceSet.class));
				}
			}))
			.withComponent(IsComponent.tag())
			.withComponent(createdUsing(of(JniLibraryComponentInternal.class), () -> new JniLibraryComponentInternal(identifier, GroupId.of(project::getGroup), project.getObjects(), project.getExtensions().getByType(TaskRegistry.class))))
			.action(ModelActionWithInputs.of(ModelComponentReference.of(ModelPath.class), ModelComponentReference.of(ModelState.class), new ModelActionWithInputs.A2<ModelPath, ModelState>() {
				private boolean alreadyExecuted = false;

				@Override
				public void execute(ModelNode entity, ModelPath path, ModelState state) {
					if (entityPath.equals(path) && state.equals(ModelState.Registered) && !alreadyExecuted) {
						alreadyExecuted = true;
						val registry = project.getExtensions().getByType(ModelRegistry.class);

//						registry.register(project.getExtensions().getByType(CHeaderSetRegistrationFactory.class).create(LanguageSourceSetIdentifier.of(identifier, "jni")));

						// TODO: ONLY if applying include language plugin
//						registry.register(project.getExtensions().getByType(CHeaderSetRegistrationFactory.class).create(LanguageSourceSetIdentifier.of(identifier, "headers")));

						registry.register(project.getExtensions().getByType(ComponentSourcesPropertyRegistrationFactory.class).create(ModelPropertyIdentifier.of(identifier, "sources"), JavaNativeInterfaceLibrarySources.class));

						project.getPluginManager().withPlugin("groovy", ignored -> {
							registry.register(project.getExtensions().getByType(GroovySourceSetRegistrationFactory.class).create(LanguageSourceSetIdentifier.of(identifier, "groovy")));
						});
						project.getPluginManager().withPlugin("java", ignored -> {
							registry.register(project.getExtensions().getByType(JavaSourceSetRegistrationFactory.class).create(LanguageSourceSetIdentifier.of(identifier, "java")));
						});
						project.getPluginManager().withPlugin("org.jetbrains.kotlin.jvm", ignored -> {
							registry.register(project.getExtensions().getByType(KotlinSourceSetRegistrationFactory.class).create(LanguageSourceSetIdentifier.of(identifier, "kotlin")));
						});

						val bucketFactory = new DeclarableDependencyBucketRegistrationFactory(NamedDomainObjectRegistry.of(project.getConfigurations()), new FrameworkAwareDependencyBucketFactory(project.getObjects(), new DefaultDependencyBucketFactory(NamedDomainObjectRegistry.of(project.getConfigurations()), DependencyFactory.forProject(project))));
						registry.register(project.getExtensions().getByType(ComponentDependenciesPropertyRegistrationFactory.class).create(ModelPropertyIdentifier.of(identifier, "dependencies"), JavaNativeInterfaceLibraryComponentDependencies.class, ModelBackedJavaNativeInterfaceLibraryComponentDependencies::new));
						val api = registry.register(bucketFactory.create(DependencyBucketIdentifier.of(declarable("api"), identifier)));
						val implementation = registry.register(bucketFactory.create(DependencyBucketIdentifier.of(declarable("jvmImplementation"), identifier)));
						registry.register(bucketFactory.create(DependencyBucketIdentifier.of(declarable("jvmRuntimeOnly"), identifier)));
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
						val runtimeElements = registry.register(consumableFactory.create(DependencyBucketIdentifier.of(consumable("runtimeElements"), identifier)));
						runtimeElements.configure(Configuration.class, configureAttributes(builder -> builder.usage(project.getObjects().named(Usage.class, Usage.JAVA_RUNTIME)).attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, project.getObjects().named(LibraryElements.class, LibraryElements.JAR))));
						runtimeElements.configure(Configuration.class, configureExtendsFrom(api.as(Configuration.class)));

						registry.register(project.getExtensions().getByType(ComponentVariantsPropertyRegistrationFactory.class).create(ModelPropertyIdentifier.of(identifier, "variants"), JniLibrary.class));

						registry.register(project.getExtensions().getByType(ComponentTasksPropertyRegistrationFactory.class).create(ModelPropertyIdentifier.of(identifier, "tasks")));

						val assembleTask = registry.register(project.getExtensions().getByType(TaskRegistrationFactory.class).create(TaskIdentifier.of(TaskName.of(ASSEMBLE_TASK_NAME), identifier), Task.class).build());
						assembleTask.configure(Task.class, configureBuildGroup());
						assembleTask.configure(Task.class, configureDescription("Assembles the outputs of %s.", identifier));

						val registerJvmJarBinaryAction = new Action<AppliedPlugin>() {
							private boolean alreadyExecuted = false;

							@Override
							public void execute(AppliedPlugin ignored) {
								if (!alreadyExecuted) {
									alreadyExecuted = true;
									registry.register(project.getExtensions().getByType(JvmJarBinaryRegistrationFactory.class).create(BinaryIdentifier.of(identifier, BinaryIdentity.ofMain("jvmJar", "JVM JAR binary"))));
								}
							}
						};
						project.getPluginManager().withPlugin("java", registerJvmJarBinaryAction);
						project.getPluginManager().withPlugin("groovy", registerJvmJarBinaryAction);
						project.getPluginManager().withPlugin("org.jetbrains.kotlin.jvm", registerJvmJarBinaryAction);

						registry.register(project.getExtensions().getByType(ComponentBinariesPropertyRegistrationFactory.class).create(ModelPropertyIdentifier.of(identifier, "binaries")));

						val dimensions = project.getExtensions().getByType(DimensionPropertyRegistrationFactory.class);
						val buildVariants = entity.addComponent(new BuildVariants(entity, project.getProviders(), project.getObjects()));
						val toolChainSelectorInternal = project.getObjects().newInstance(ToolChainSelectorInternal.class);
						registry.register(dimensions.newAxisProperty(path.child("targetMachines"))
							.axis(TARGET_MACHINE_COORDINATE_AXIS)
							.defaultValue(TargetMachines.host())
							.validateUsing((Iterable<TargetMachine> it) -> assertTargetMachinesAreKnown(it, toolChainSelectorInternal))
							.build());
						registry.register(dimensions.newAxisProperty(path.child("targetLinkages"))
							.elementType(TargetLinkage.class)
							.axis(BinaryLinkage.BINARY_LINKAGE_COORDINATE_AXIS)
							.defaultValue(TargetLinkages.SHARED)
							.build());
						registry.register(dimensions.buildVariants(path.child("buildVariants"), buildVariants.get()));
					}
				}
			}))
			.action(ModelActionWithInputs.of(ModelComponentReference.of(ComponentIdentifier.class), ModelComponentReference.of(ModelState.IsAtLeastFinalized.class), (entity, id, ignored) -> {
				if (id.equals(identifier)) {
					val component = ModelNodeUtils.get(entity, JniLibraryComponentInternal.class);
					component.getDevelopmentVariant().convention(project.getProviders().provider(new BuildableDevelopmentVariantConvention<>(component.getVariants()::get)));

					component.finalizeValue();

					val variants = ImmutableMap.<BuildVariant, ModelNode>builder();
					component.getBuildVariants().get().forEach(buildVariant -> {
						val variantIdentifier = VariantIdentifier.builder().withBuildVariant(buildVariant).withComponentIdentifier(component.getIdentifier()).withType(JniLibraryInternal.class).build();
						val variant = project.getExtensions().getByType(ModelRegistry.class).register(variantFactory.create(variantIdentifier));

						ModelStates.realize(ModelNodes.of(variant)); // FIXME: Remove once the refactoring is over

						variants.put(buildVariant, ModelNodes.of(variant));
					});
					entity.addComponent(new Variants(variants.build()));
				}
			}))

			// TODO: This convention is only good is to support older Gradle style source set
			//   We could deprecate this behaviour by creating additional source set named objc/objcpp and warn when there's sources in them... that would fail some tests because we don't expect a source set... we could also just remove it...
			.action(ModelActionWithInputs.of(ModelComponentReference.of(ModelPath.class), ModelComponentReference.ofAny(projectionOf(ObjectiveCSourceSet.class)), ModelComponentReference.of(ModelState.IsAtLeastRealized.class), (entity, path, projection, ignored) -> {
				if (entityPath.isDescendant(path)) {
					withConventionOf(maven(identifier.getName()), defaultObjectiveCGradle(identifier.getName())).accept(ModelNodeUtils.get(entity, LanguageSourceSet.class));
				}
			}))
			.action(ModelActionWithInputs.of(ModelComponentReference.of(ModelPath.class), ModelComponentReference.ofAny(projectionOf(ObjectiveCppSourceSet.class)), ModelComponentReference.of(ModelState.IsAtLeastRealized.class), (entity, path, projection, ignored) -> {
				if (entityPath.isDescendant(path)) {
					withConventionOf(maven(identifier.getName()), defaultObjectiveCppGradle(identifier.getName())).accept(ModelNodeUtils.get(entity, LanguageSourceSet.class));
				}
			}))
			.build()
			;
	}

	private static void assertTargetMachinesAreKnown(Iterable<TargetMachine> targetMachines, ToolChainSelectorInternal toolChainSelector) {
		List<TargetMachine> unknownTargetMachines = Streams.stream(targetMachines).filter(it -> !toolChainSelector.isKnown(it)).collect(Collectors.toList());
		if (!unknownTargetMachines.isEmpty()) {
			throw new IllegalArgumentException("The following target machines are not know by the defined tool chains:\n" + unknownTargetMachines.stream().map(it -> " * " + it.getOperatingSystemFamily().getCanonicalName() + " " + it.getArchitecture().getCanonicalName()).collect(joining("\n")));
		}
	}

	public static abstract class DefaultJavaNativeInterfaceLibrary implements JavaNativeInterfaceLibrary
		, ModelBackedDependencyAwareComponentMixIn<JavaNativeInterfaceLibraryComponentDependencies>
		, ModelBackedVariantAwareComponentMixIn<JniLibrary>
		, ModelBackedSourceAwareComponentMixIn<JavaNativeInterfaceLibrarySources>
		, ModelBackedBinaryAwareComponentMixIn
		, ModelBackedTaskAwareComponentMixIn
		, ModelBackedNamedMixIn
	{
	}
}
