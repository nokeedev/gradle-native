/*
 * Copyright 2020 the original author or authors.
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
package dev.nokee.testing.nativebase.internal.plugins;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.objectivec.ObjectiveCSourceSet;
import dev.nokee.language.objectivecpp.ObjectiveCppSourceSet;
import dev.nokee.language.swift.SwiftSourceSet;
import dev.nokee.model.DependencyFactory;
import dev.nokee.model.DomainObjectProvider;
import dev.nokee.model.NamedDomainObjectRegistry;
import dev.nokee.model.PolymorphicDomainObjectRegistry;
import dev.nokee.model.internal.DomainObjectEventPublisher;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.core.*;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.model.internal.type.TypeOf;
import dev.nokee.platform.base.BuildVariant;
import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.base.internal.*;
import dev.nokee.platform.base.internal.binaries.BinaryConfigurer;
import dev.nokee.platform.base.internal.binaries.BinaryRepository;
import dev.nokee.platform.base.internal.binaries.BinaryViewFactory;
import dev.nokee.platform.base.internal.dependencies.*;
import dev.nokee.platform.base.internal.tasks.TaskIdentifier;
import dev.nokee.platform.base.internal.tasks.TaskName;
import dev.nokee.platform.base.internal.tasks.TaskRegistry;
import dev.nokee.platform.base.internal.tasks.TaskViewFactory;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import dev.nokee.platform.nativebase.internal.ExecutableBinaryInternal;
import dev.nokee.platform.nativebase.internal.dependencies.*;
import dev.nokee.platform.nativebase.internal.rules.BuildableDevelopmentVariantConvention;
import dev.nokee.platform.nativebase.internal.rules.RegisterAssembleLifecycleTaskRule;
import dev.nokee.platform.objectivec.ObjectiveCApplicationSources;
import dev.nokee.testing.base.TestSuiteContainer;
import dev.nokee.testing.base.internal.IsTestComponent;
import dev.nokee.testing.base.internal.plugins.TestingBasePlugin;
import dev.nokee.testing.nativebase.NativeTestSuite;
import dev.nokee.testing.nativebase.NativeTestSuiteVariant;
import dev.nokee.testing.nativebase.internal.DefaultNativeTestSuiteComponent;
import dev.nokee.testing.nativebase.internal.DefaultNativeTestSuiteVariant;
import lombok.val;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

import java.util.Optional;

import static dev.nokee.model.internal.core.ModelActions.once;
import static dev.nokee.model.internal.core.ModelComponentType.componentOf;
import static dev.nokee.model.internal.core.ModelComponentType.projectionOf;
import static dev.nokee.model.internal.core.ModelNodeUtils.applyTo;
import static dev.nokee.model.internal.core.ModelNodes.discover;
import static dev.nokee.model.internal.core.ModelNodes.stateAtLeast;
import static dev.nokee.model.internal.core.ModelProjections.createdUsing;
import static dev.nokee.model.internal.core.NodePredicate.allDirectDescendants;
import static dev.nokee.model.internal.core.NodePredicate.self;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.platform.base.internal.LanguageSourceSetConventionSupplier.*;
import static org.gradle.language.base.plugins.LifecycleBasePlugin.ASSEMBLE_TASK_NAME;

public class NativeUnitTestingPlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply("lifecycle-base");
		project.getPluginManager().apply(TestingBasePlugin.class);

		val testSuites = project.getExtensions().getByType(TestSuiteContainer.class);
		val registry = ModelNodeUtils.get(ModelNodes.of(testSuites), NodeRegistrationFactoryRegistry.class);
		registry.registerFactory(of(NativeTestSuite.class), (ModelRegistrationFactory) name -> nativeTestSuite(name, project));

		project.afterEvaluate(proj -> {
			// TODO: We delay as late as possible to "fake" a finalize action.
			testSuites.configureEach(DefaultNativeTestSuiteComponent.class, it -> {
				ModelStates.finalize(it.getNode());
			});
		});
	}

	public static ModelRegistration nativeTestSuite(String name, Project project) {
		val identifier = ComponentIdentifier.builder().name(ComponentName.of(name)).displayName("native test suite").withProjectIdentifier(ProjectIdentifier.of(project)).build();
		val entityPath = ModelPath.path(identifier.getName().get());
		return ModelRegistration.builder()
			.withComponent(entityPath)
			.withComponent(createdUsing(of(DefaultNativeTestSuiteComponent.class), () -> new DefaultNativeTestSuiteComponent(identifier, project.getObjects(), project.getProviders(), project.getTasks(), project.getExtensions().getByType(DomainObjectEventPublisher.class), project.getExtensions().getByType(TaskRegistry.class), project.getExtensions().getByType(TaskViewFactory.class), project.getExtensions().getByType(ModelLookup.class))))
			.withComponent(IsTestComponent.tag())
			.withComponent(IsComponent.tag())
			.withComponent(identifier)
			.action(ModelActionWithInputs.of(ModelComponentReference.of(ModelPath.class), ModelComponentReference.ofAny(projectionOf(LanguageSourceSet.class)), ModelComponentReference.of(ModelState.IsAtLeastRealized.class), (entity, path, projection, ignored) -> {
				if (entityPath.isDescendant(path)) {
					withConventionOf(maven(identifier.getName())).accept(ModelNodeUtils.get(entity, LanguageSourceSet.class));
				}
			}))
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
			// TODO: Choose a better component sources
			.action(ModelActionWithInputs.of(ModelComponentReference.of(ModelPath.class), ModelComponentReference.of(ModelState.class), new ModelActionWithInputs.A2<ModelPath, ModelState>() {
				private boolean alreadyExecuted = false;
				@Override
				public void execute(ModelNode entity, ModelPath path, ModelState state) {
					if (entityPath.equals(path) && state.equals(ModelState.Registered) && !alreadyExecuted) {
						alreadyExecuted = true;
						val registry = project.getExtensions().getByType(ModelRegistry.class);

						registry.register(project.getExtensions().getByType(ComponentSourcesPropertyRegistrationFactory.class).create(path.child("sources"), ObjectiveCApplicationSources.class));

						registry.register(project.getExtensions().getByType(ComponentBinariesPropertyRegistrationFactory.class).create(path.child("binaries")));

						registry.register(project.getExtensions().getByType(ComponentTasksPropertyRegistrationFactory.class).create(path.child("tasks")));

						val bucketFactory = new DeclarableDependencyBucketRegistrationFactory(NamedDomainObjectRegistry.of(project.getConfigurations()), new FrameworkAwareDependencyBucketFactory(project.getObjects(), new DependencyBucketFactoryImpl(NamedDomainObjectRegistry.of(project.getConfigurations()), DependencyFactory.forProject(project))));
						registry.register(project.getExtensions().getByType(ComponentDependenciesPropertyRegistrationFactory.class).create(path.child("dependencies"), NativeComponentDependencies.class, ModelBackedNativeComponentDependencies::new));

						registry.register(bucketFactory.create(path.child("implementation"), DependencyBucketIdentifier.of(DependencyBucketName.of("implementation"), DeclarableDependencyBucket.class, identifier)));
						registry.register(bucketFactory.create(path.child("compileOnly"), DependencyBucketIdentifier.of(DependencyBucketName.of("compileOnly"), DeclarableDependencyBucket.class, identifier)));
						registry.register(bucketFactory.create(path.child("linkOnly"), DependencyBucketIdentifier.of(DependencyBucketName.of("linkOnly"), DeclarableDependencyBucket.class, identifier)));
						registry.register(bucketFactory.create(path.child("runtimeOnly"), DependencyBucketIdentifier.of(DependencyBucketName.of("runtimeOnly"), DeclarableDependencyBucket.class, identifier)));

						registry.register(project.getExtensions().getByType(ComponentVariantsPropertyRegistrationFactory.class).create(path.child("variants"), NativeTestSuiteVariant.class));

						registry.register(ModelRegistration.builder()
							.withComponent(path.child("developmentVariant"))
							.withComponent(IsModelProperty.tag())
							.withComponent(createdUsing(of(new TypeOf<Property<NativeTestSuiteVariant>>() {}), () -> project.getObjects().property(NativeTestSuiteVariant.class)))
							.build());
					}
				}
			}))
			.action(new RegisterAssembleLifecycleTaskRule(identifier, PolymorphicDomainObjectRegistry.of(project.getTasks()), project.getExtensions().getByType(ModelRegistry.class), project.getProviders()))
			.action(ModelActionWithInputs.of(ModelComponentReference.of(ModelPath.class), ModelComponentReference.of(ModelState.IsAtLeastFinalized.class), (entity, path, ignored) -> {
				if (entityPath.equals(path)) {
					val registry = project.getExtensions().getByType(ModelRegistry.class);
					val component = ModelNodeUtils.get(entity, DefaultNativeTestSuiteComponent.class);
					component.finalizeExtension(project);
					component.getDevelopmentVariant().convention(project.getProviders().provider(new BuildableDevelopmentVariantConvention<>(() -> component.getVariants().get())));

					val variants = ImmutableMap.<BuildVariant, ModelNode>builder();
					component.getBuildVariants().get().forEach(buildVariant -> {
						val variantIdentifier = VariantIdentifier.builder().withBuildVariant(buildVariant).withComponentIdentifier(component.getIdentifier()).withType(DefaultNativeTestSuiteVariant.class).build();

						val variant = ModelNodeUtils.register(entity, nativeTestSuiteVariant(variantIdentifier, component, project));
						variants.put(buildVariant, ModelNodes.of(variant));
						onEachVariantDependencies(variant.as(DefaultNativeTestSuiteVariant.class), ModelNodes.of(variant).getComponent(componentOf(VariantComponentDependencies.class)));
					});
					entity.addComponent(new Variants(variants.build()));
				}
			}))
			.build()
			;
	}

	private static NodeRegistration nativeTestSuiteVariant(VariantIdentifier<DefaultNativeTestSuiteVariant> variantIdentifier, DefaultNativeTestSuiteComponent component, Project project) {
		val taskRegistry = project.getExtensions().getByType(TaskRegistry.class);
		val assembleTask = taskRegistry.registerIfAbsent(TaskIdentifier.of(TaskName.of(ASSEMBLE_TASK_NAME), variantIdentifier));
		val variantDependencies = newDependencies((BuildVariantInternal) variantIdentifier.getBuildVariant(), variantIdentifier, component, project.getObjects(), project.getConfigurations(), DependencyFactory.forProject(project), project.getExtensions().getByType(ModelLookup.class));
		return NodeRegistration.unmanaged(variantIdentifier.getUnambiguousName(), of(DefaultNativeTestSuiteVariant.class), () -> {
			return project.getObjects().newInstance(DefaultNativeTestSuiteVariant.class, variantIdentifier, project.getObjects(), project.getProviders(), assembleTask, project.getExtensions().getByType(BinaryViewFactory.class));
		})
			.withComponent(IsVariant.tag())
			.withComponent(variantDependencies)
			.withComponent(variantIdentifier)
			.action(self(discover()).apply(ModelActionWithInputs.of(ModelComponentReference.of(ModelPath.class), (entity, path) -> {
				val registry = project.getExtensions().getByType(ModelRegistry.class);

				val binaryRepository = project.getExtensions().getByType(BinaryRepository.class);
				val binaryIdentifier = BinaryIdentifier.of(BinaryName.of("executable"), ExecutableBinaryInternal.class, variantIdentifier);
				val binaryEntity = registry.register(ModelRegistration.builder()
					.withComponent(path.child("executable"))
					.withComponent(IsBinary.tag())
					.withComponent(binaryIdentifier)
					.withComponent(createdUsing(of(ExecutableBinaryInternal.class), () -> binaryRepository.get(binaryIdentifier)))
					.build());
				project.getExtensions().getByType(BinaryConfigurer.class)
					.configure(binaryIdentifier, binary -> ModelStates.realize(ModelNodes.of(binaryEntity)));

				registry.register(project.getExtensions().getByType(ComponentBinariesPropertyRegistrationFactory.class).create(path.child("binaries")));

				registry.register(project.getExtensions().getByType(ComponentTasksPropertyRegistrationFactory.class).create(path.child("tasks")));

				val dependencies = variantDependencies.getDependencies();
				registry.register(project.getExtensions().getByType(ComponentDependenciesPropertyRegistrationFactory.class).create(path.child("dependencies"), dependencies));

				registry.register(ModelRegistration.builder()
					.withComponent(path.child("implementation"))
					.withComponent(IsDependencyBucket.tag())
					.withComponent(createdUsing(of(Configuration.class), () -> dependencies.getImplementation().getAsConfiguration()))
					.withComponent(createdUsing(of(DependencyBucket.class), () -> dependencies.getImplementation()))
					.withComponent(createdUsing(of(NamedDomainObjectProvider.class), () -> project.getConfigurations().named(dependencies.getImplementation().getAsConfiguration().getName())))
					.build());
				registry.register(ModelRegistration.builder()
					.withComponent(path.child("compileOnly"))
					.withComponent(IsDependencyBucket.tag())
					.withComponent(createdUsing(of(Configuration.class), () -> dependencies.getCompileOnly().getAsConfiguration()))
					.withComponent(createdUsing(of(DependencyBucket.class), () -> dependencies.getCompileOnly()))
					.withComponent(createdUsing(of(NamedDomainObjectProvider.class), () -> project.getConfigurations().named(dependencies.getCompileOnly().getAsConfiguration().getName())))
					.build());
				registry.register(ModelRegistration.builder()
					.withComponent(path.child("linkOnly"))
					.withComponent(IsDependencyBucket.tag())
					.withComponent(createdUsing(of(Configuration.class), () -> dependencies.getLinkOnly().getAsConfiguration()))
					.withComponent(createdUsing(of(DependencyBucket.class), () -> dependencies.getLinkOnly()))
					.withComponent(createdUsing(of(NamedDomainObjectProvider.class), () -> project.getConfigurations().named(dependencies.getLinkOnly().getAsConfiguration().getName())))
					.build());
				registry.register(ModelRegistration.builder()
					.withComponent(path.child("runtimeOnly"))
					.withComponent(IsDependencyBucket.tag())
					.withComponent(createdUsing(of(Configuration.class), () -> dependencies.getRuntimeOnly().getAsConfiguration()))
					.withComponent(createdUsing(of(DependencyBucket.class), () -> dependencies.getRuntimeOnly()))
					.withComponent(createdUsing(of(NamedDomainObjectProvider.class), () -> project.getConfigurations().named(dependencies.getRuntimeOnly().getAsConfiguration().getName())))
					.build());

				whenElementKnown(entity, ModelActionWithInputs.of(ModelComponentReference.ofAny(projectionOf(Configuration.class)), ModelComponentReference.of(ModelPath.class), (e, ignored, p) -> {
					((NamedDomainObjectProvider<Configuration>) ModelNodeUtils.get(e, NamedDomainObjectProvider.class)).configure(configuration -> {
						val parentConfigurationResult = project.getExtensions().getByType(ModelLookup.class).query(ModelSpecs.of(ModelNodes.withPath(path.getParent().get().child(p.getName()))));
						Optional.ofNullable(Iterables.getOnlyElement(parentConfigurationResult.get(), null)).ifPresent(parentConfigurationEntity -> {
							val parentConfiguration = ModelNodeUtils.get(parentConfigurationEntity, Configuration.class);
							if (!parentConfiguration.getName().equals(configuration.getName())) {
								configuration.extendsFrom(parentConfiguration);
							}
						});
					});
				}));
			})))
			;
	}

	private static void whenElementKnown(Object target, ModelAction action) {
		applyTo(ModelNodes.of(target), allDirectDescendants(stateAtLeast(ModelState.Created)).apply(once(action)));
	}

	private static VariantComponentDependencies<DefaultNativeComponentDependencies> newDependencies(BuildVariantInternal buildVariant, VariantIdentifier<DefaultNativeTestSuiteVariant> variantIdentifier, DefaultNativeTestSuiteComponent component, ObjectFactory objectFactory, ConfigurationContainer configurationContainer, DependencyFactory dependencyFactory, ModelLookup modelLookup) {
		val dependencyContainer = objectFactory.newInstance(DefaultComponentDependencies.class, variantIdentifier, new DependencyBucketFactoryImpl(NamedDomainObjectRegistry.of(configurationContainer), dependencyFactory));
		val variantDependencies = objectFactory.newInstance(DefaultNativeComponentDependencies.class, dependencyContainer);

		val incomingDependenciesBuilder = DefaultNativeIncomingDependencies.builder(variantDependencies).withVariant(buildVariant).withOwnerIdentifier(variantIdentifier).withBucketFactory(new DependencyBucketFactoryImpl(NamedDomainObjectRegistry.of(configurationContainer), dependencyFactory));
		boolean hasSwift = modelLookup.anyMatch(ModelSpecs.of(ModelNodes.withType(of(SwiftSourceSet.class))));
		if (hasSwift) {
			incomingDependenciesBuilder.withIncomingSwiftModules();
		} else {
			incomingDependenciesBuilder.withIncomingHeaders();
		}

		NativeIncomingDependencies incoming = incomingDependenciesBuilder.buildUsing(objectFactory);
		NativeOutgoingDependencies outgoing = objectFactory.newInstance(NativeApplicationOutgoingDependencies.class, variantIdentifier, buildVariant, variantDependencies);

		return new VariantComponentDependencies<>(variantDependencies, incoming, outgoing);
	}

	private static void onEachVariantDependencies(DomainObjectProvider<DefaultNativeTestSuiteVariant> variant, VariantComponentDependencies<?> dependencies) {
		dependencies.getOutgoing().getExportedBinary().convention(variant.flatMap(it -> it.getDevelopmentBinary()));
	}
}
