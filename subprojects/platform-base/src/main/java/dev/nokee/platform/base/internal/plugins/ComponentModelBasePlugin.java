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
package dev.nokee.platform.base.internal.plugins;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeToken;
import dev.nokee.internal.Factory;
import dev.nokee.model.capabilities.variants.CreateVariantsRule;
import dev.nokee.model.capabilities.variants.IsVariant;
import dev.nokee.model.capabilities.variants.KnownVariantInformationElement;
import dev.nokee.model.internal.DefaultModelObjectIdentifier;
import dev.nokee.model.internal.ModelElementSupport;
import dev.nokee.model.internal.ModelMapAdapters;
import dev.nokee.model.internal.ModelObjectIdentifier;
import dev.nokee.model.internal.buffers.ModelBuffers;
import dev.nokee.model.internal.core.DisplayNameComponent;
import dev.nokee.model.internal.core.GradlePropertyComponent;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelComponentReference;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodeContext;
import dev.nokee.model.internal.core.ModelNodeUtils;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.core.ModelPathComponent;
import dev.nokee.model.internal.core.ParentComponent;
import dev.nokee.model.internal.names.ElementNameComponent;
import dev.nokee.model.internal.plugins.ModelBasePlugin;
import dev.nokee.model.internal.registry.ModelConfigurer;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.model.internal.tags.ModelTags;
import dev.nokee.model.internal.type.ModelType;
import dev.nokee.model.internal.type.TypeOf;
import dev.nokee.platform.base.Artifact;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.platform.base.BuildVariant;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.ComponentContainer;
import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.base.HasBaseName;
import dev.nokee.platform.base.TaskView;
import dev.nokee.platform.base.Variant;
import dev.nokee.platform.base.VariantAwareComponent;
import dev.nokee.platform.base.VariantView;
import dev.nokee.platform.base.internal.BinaryViewAdapter;
import dev.nokee.platform.base.internal.BuildVariants;
import dev.nokee.platform.base.internal.BuildVariantsPropertyComponent;
import dev.nokee.platform.base.internal.ComponentContainerAdapter;
import dev.nokee.platform.base.internal.ComponentTasksPropertyRegistrationFactory;
import dev.nokee.platform.base.internal.DimensionPropertyRegistrationFactory;
import dev.nokee.platform.base.internal.IsBinary;
import dev.nokee.platform.base.internal.IsComponent;
import dev.nokee.platform.base.internal.IsDependencyBucket;
import dev.nokee.platform.base.internal.IsTask;
import dev.nokee.platform.base.internal.MainProjectionComponent;
import dev.nokee.platform.base.internal.ModelBackedVariantAwareComponentMixIn;
import dev.nokee.platform.base.internal.ModelBackedVariantDimensions;
import dev.nokee.platform.base.internal.ModelNodeBackedViewStrategy;
import dev.nokee.platform.base.internal.ModelObjectFactory;
import dev.nokee.platform.base.internal.TaskViewAdapter;
import dev.nokee.platform.base.internal.VariantViewAdapter;
import dev.nokee.platform.base.internal.ViewAdapter;
import dev.nokee.platform.base.internal.assembletask.AssembleTaskCapabilityPlugin;
import dev.nokee.platform.base.internal.dependencies.ConsumableDependencyBucketSpec;
import dev.nokee.platform.base.internal.dependencies.DeclarableDependencyBucketSpec;
import dev.nokee.platform.base.internal.dependencies.DependencyBucketCapabilityPlugin;
import dev.nokee.platform.base.internal.dependencies.ResolvableDependencyBucketSpec;
import dev.nokee.platform.base.internal.elements.ComponentElementsCapabilityPlugin;
import dev.nokee.platform.base.internal.elements.ComponentElementsPropertyRegistrationFactory;
import dev.nokee.platform.base.internal.extensionaware.ExtensionAwareCapability;
import dev.nokee.platform.base.internal.project.ProjectCapabilityPlugin;
import dev.nokee.platform.base.internal.project.ProjectProjectionComponent;
import dev.nokee.platform.base.internal.tasks.TaskCapabilityPlugin;
import lombok.val;
import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer;
import org.gradle.api.Named;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.provider.Provider;

import java.lang.reflect.ParameterizedType;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static dev.nokee.model.internal.core.ModelPath.root;
import static dev.nokee.model.internal.core.ModelProjections.createdUsing;
import static dev.nokee.model.internal.core.ModelRegistration.builder;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.factoryRegistryOf;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.mapOf;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.model;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.objects;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.registryOf;
import static dev.nokee.model.internal.type.ModelType.of;

public class ComponentModelBasePlugin implements Plugin<Project> {
	private static final org.gradle.api.reflect.TypeOf<ExtensiblePolymorphicDomainObjectContainer<Component>> COMPONENT_CONTAINER_TYPE = new org.gradle.api.reflect.TypeOf<ExtensiblePolymorphicDomainObjectContainer<Component>>() {};
	private static final org.gradle.api.reflect.TypeOf<ExtensiblePolymorphicDomainObjectContainer<Variant>> VARIANT_CONTAINER_TYPE = new org.gradle.api.reflect.TypeOf<ExtensiblePolymorphicDomainObjectContainer<Variant>>() {};
	private static final org.gradle.api.reflect.TypeOf<ExtensiblePolymorphicDomainObjectContainer<DependencyBucket>> DEPENDENCY_BUCKET_CONTAINER_TYPE = new org.gradle.api.reflect.TypeOf<ExtensiblePolymorphicDomainObjectContainer<DependencyBucket>>() {};
	private static final org.gradle.api.reflect.TypeOf<ExtensiblePolymorphicDomainObjectContainer<Artifact>> ARTIFACT_CONTAINER_TYPE = new org.gradle.api.reflect.TypeOf<ExtensiblePolymorphicDomainObjectContainer<Artifact>>() {};

	public static ExtensiblePolymorphicDomainObjectContainer<Component> components(ExtensionAware project) {
		return project.getExtensions().getByType(COMPONENT_CONTAINER_TYPE);
	}

	public static ExtensiblePolymorphicDomainObjectContainer<Variant> variants(ExtensionAware project) {
		return project.getExtensions().getByType(VARIANT_CONTAINER_TYPE);
	}

	public static ExtensiblePolymorphicDomainObjectContainer<DependencyBucket> dependencyBuckets(ExtensionAware target) {
		return target.getExtensions().getByType(DEPENDENCY_BUCKET_CONTAINER_TYPE);
	}

	public static ExtensiblePolymorphicDomainObjectContainer<Artifact> artifacts(ExtensionAware project) {
		return project.getExtensions().getByType(ARTIFACT_CONTAINER_TYPE);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void apply(Project project) {
		project.getPluginManager().apply(ModelBasePlugin.class);
		project.getPluginManager().apply("lifecycle-base");

		val modeRegistry = project.getExtensions().getByType(ModelRegistry.class);
		val modelLookup = project.getExtensions().getByType(ModelLookup.class);

		project.getExtensions().getByType(ModelLookup.class).get(root()).addComponent(new ProjectProjectionComponent(project));

		project.getExtensions().add(ComponentTasksPropertyRegistrationFactory.class, "__nokee_componentTasksPropertyFactory", new ComponentTasksPropertyRegistrationFactory());

		project.getExtensions().add(COMPONENT_CONTAINER_TYPE, "$components", project.getObjects().polymorphicDomainObjectContainer(Component.class));
		project.getExtensions().add(VARIANT_CONTAINER_TYPE, "$variants", project.getObjects().polymorphicDomainObjectContainer(Variant.class));
		project.getExtensions().add(DEPENDENCY_BUCKET_CONTAINER_TYPE, "$dependencyBuckets", project.getObjects().polymorphicDomainObjectContainer(DependencyBucket.class));
		project.getExtensions().add(ARTIFACT_CONTAINER_TYPE, "$artifacts", project.getObjects().polymorphicDomainObjectContainer(Artifact.class));

		model(project, objects()).register(model(project).getExtensions().create("components", ModelMapAdapters.ForExtensiblePolymorphicDomainObjectContainer.class, Component.class, components(project)));
		model(project, objects()).register(model(project).getExtensions().create("variants", ModelMapAdapters.ForExtensiblePolymorphicDomainObjectContainer.class, Variant.class, variants(project)));
		model(project, objects()).register(model(project).getExtensions().create("dependencyBuckets", ModelMapAdapters.ForExtensiblePolymorphicDomainObjectContainer.class, DependencyBucket.class, dependencyBuckets(project)));
		model(project, objects()).register(model(project).getExtensions().create("artifacts", ModelMapAdapters.ForExtensiblePolymorphicDomainObjectContainer.class, Artifact.class, artifacts(project)));

		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelComponentReference.of(MainProjectionComponent.class), ModelComponentReference.of(ModelState.IsAtLeastRealized.class), (entity, mainProjection, ignored) -> {
			ModelNodeUtils.get(entity, mainProjection.getProjectionType()); // realize provider
		}));

		project.getExtensions().getByType(ModelConfigurer.class).configure(new DomainObjectRegistration<Component>(IsComponent.class, model(project, registryOf(Component.class))));
		project.getExtensions().getByType(ModelConfigurer.class).configure(new DomainObjectRegistration<Binary>(IsBinary.class, model(project, registryOf(Artifact.class))));
		project.getExtensions().getByType(ModelConfigurer.class).configure(new DomainObjectRegistration<Variant>(IsVariant.class, model(project, registryOf(Variant.class))));
		project.getExtensions().getByType(ModelConfigurer.class).configure(new DomainObjectRegistration<DependencyBucket>(IsDependencyBucket.class, model(project, registryOf(DependencyBucket.class))));

		// FIXME: This is temporary until we convert all entity
		project.afterEvaluate(__ -> {
			model(project, mapOf(Variant.class)).whenElementKnow(it -> it.realizeNow()); // Because outgoing configuration are created when variant realize
		});
		project.afterEvaluate(__ -> {
			int previousCount = 0;
			List<ModelNode> result = Collections.emptyList();
			do
			{
				previousCount = result.size();
				result = project.getExtensions().getByType(ModelLookup.class).query(it -> it.has(MainProjectionComponent.class)).get();
				result.forEach(it -> {
					it.find(MainProjectionComponent.class).ifPresent(component -> {
						ModelStates.finalize(it);
					});
				});
			} while (previousCount != result.size());
		});

		model(project, factoryRegistryOf(DependencyBucket.class)).registerFactory(ConsumableDependencyBucketSpec.class, new ModelObjectFactory<ConsumableDependencyBucketSpec>(project, IsDependencyBucket.class) {
			@Override
			protected ConsumableDependencyBucketSpec doCreate(String name) {
				return project.getObjects().newInstance(ConsumableDependencyBucketSpec.class, model(project, registryOf(Configuration.class)));
			}
		});
		model(project, factoryRegistryOf(DependencyBucket.class)).registerFactory(ResolvableDependencyBucketSpec.class, new ModelObjectFactory<ResolvableDependencyBucketSpec>(project, IsDependencyBucket.class) {
			@Override
			protected ResolvableDependencyBucketSpec doCreate(String name) {
				return project.getObjects().newInstance(ResolvableDependencyBucketSpec.class, model(project, registryOf(Configuration.class)));
			}
		});
		model(project, factoryRegistryOf(DependencyBucket.class)).registerFactory(DeclarableDependencyBucketSpec.class, new ModelObjectFactory<DeclarableDependencyBucketSpec>(project, IsDependencyBucket.class) {
			@Override
			protected DeclarableDependencyBucketSpec doCreate(String name) {
				return project.getObjects().newInstance(DeclarableDependencyBucketSpec.class, model(project, registryOf(Configuration.class)));
			}
		});

		project.getPluginManager().apply(DependencyBucketCapabilityPlugin.class);
		project.getPluginManager().apply(TaskCapabilityPlugin.class);
		project.getPluginManager().apply(ProjectCapabilityPlugin.class);
		project.getPluginManager().apply(AssembleTaskCapabilityPlugin.class);

		final Factory<BinaryView<Binary>> binariesFactory = () -> {
			Named.Namer namer = new Named.Namer();
			Optional<ModelNode> entity = ModelNodeContext.findCurrentModelNode();
			ModelObjectIdentifier identifier = ModelElementSupport.nextIdentifier();
			Runnable realizeNow = () -> {
				entity.ifPresent(ModelStates::finalize);
			};
			return new BinaryViewAdapter<>(new ViewAdapter<>(Binary.class, new ModelNodeBackedViewStrategy(it -> namer.determineName((Binary) it), artifacts(project), project.getProviders(), project.getObjects(), realizeNow, identifier)));
		};
		project.getExtensions().add(new org.gradle.api.reflect.TypeOf<Factory<BinaryView<Binary>>>() {}, "__nokee_binariesFactory", binariesFactory);

		final Factory<TaskView<Task>> tasksFactory = () -> {
			Task.Namer namer = new Task.Namer();
			Optional<ModelNode> entity = ModelNodeContext.findCurrentModelNode();
			ModelObjectIdentifier identifier = ModelElementSupport.nextIdentifier();
			Runnable realizeNow = () -> {
				entity.ifPresent(ModelStates::finalize);
			};
			return new TaskViewAdapter<>(new ViewAdapter<>(Task.class, new ModelNodeBackedViewStrategy(it -> namer.determineName((Task) it), project.getTasks(), project.getProviders(), project.getObjects(), realizeNow, identifier)));
		};
		project.getExtensions().add(new org.gradle.api.reflect.TypeOf<Factory<TaskView<Task>>>() {}, "__nokee_tasksFactory", tasksFactory);

		project.getExtensions().add(DimensionPropertyRegistrationFactory.class, "__nokee_dimensionPropertyFactory", new DimensionPropertyRegistrationFactory(project.getObjects()));

		project.getPluginManager().apply(ExtensionAwareCapability.class);

		val elementsPropertyFactory = new ComponentElementsPropertyRegistrationFactory();
		project.getExtensions().getByType(ModelConfigurer.class).configure(new OnDiscover(ModelActionWithInputs.of(ModelComponentReference.ofProjection(ModelType.of(new TypeOf<ModelBackedVariantAwareComponentMixIn<? extends Variant>>() {})), ModelComponentReference.of(IdentifierComponent.class), ModelComponentReference.of(ParentComponent.class), (entity, component, identifier, parent) -> {
			val registry = project.getExtensions().getByType(ModelRegistry.class);
			val dimensions = project.getExtensions().getByType(DimensionPropertyRegistrationFactory.class);
			val buildVariants = entity.addComponent(new BuildVariants(entity, project.getProviders(), project.getObjects()));
			entity.addComponent(new ModelBackedVariantDimensions(entity, registry, dimensions));

			val bv = registry.register(builder().withComponent(new ElementNameComponent("buildVariants")).withComponent(new ParentComponent(entity)).mergeFrom(dimensions.buildVariants(buildVariants.get())).build());
			entity.addComponent(new BuildVariantsPropertyComponent(ModelNodes.of(bv)));

			registry.register(builder()
				.withComponent(new ElementNameComponent("variants"))
				.withComponent(new ParentComponent(entity))
				.mergeFrom(elementsPropertyFactory.newProperty().baseRef(parent.get()).elementType(of(variantType((ModelType<VariantAwareComponent<? extends Variant>>) component.getType()))).build())
				.withComponent(createdUsing(of(VariantView.class), () -> new VariantViewAdapter<>(ModelNodeUtils.get(ModelNodeContext.getCurrentModelNode(), of(new TypeOf<ViewAdapter<? extends Variant>>() {})))))
				.build());
		})));
		project.getExtensions().getByType(ModelConfigurer.class).configure(new CreateVariantsRule(project.getExtensions().getByType(ModelRegistry.class)));
		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelComponentReference.of(ModelState.IsAtLeastFinalized.class), ModelComponentReference.of(BuildVariantsPropertyComponent.class), (entity, ignored, buildVariants) -> {
			// TODO: Each plugins should just map the build variants into the variants.
			//   Sort-of, each plugins should complete the configuration but not create the variant themselves
			final ImmutableSet.Builder<KnownVariantInformationElement> builder = ImmutableSet.builder();
			((Provider<Set<BuildVariant>>) buildVariants.get().get(GradlePropertyComponent.class).get()).get().forEach(it -> {
				builder.add(new KnownVariantInformationElement(it.toString()));
			});
			entity.addComponent(ModelBuffers.of(KnownVariantInformationElement.class, builder.build()));
		}));

		// ComponentFromEntity<ParentComponent> read-only self
		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelTags.referenceOf(IsTask.class), ModelComponentReference.of(ModelPathComponent.class), ModelComponentReference.of(DisplayNameComponent.class), ModelComponentReference.of(ElementNameComponent.class), ModelComponentReference.of(ModelState.IsAtLeastCreated.class), (entity, ignored1, path, displayName, elementName, ignored2) -> {
			if (!entity.has(IdentifierComponent.class)) {
				val parentIdentifier = entity.find(ParentComponent.class).map(parent -> parent.get().get(IdentifierComponent.class).get()).orElse(null);
				entity.addComponent(new IdentifierComponent(new DefaultModelObjectIdentifier(elementName.get(), parentIdentifier)));
			}
		}));

		project.getPluginManager().apply(ComponentElementsCapabilityPlugin.class);

		val components = modeRegistry.register(builder()
			.withComponent(new ElementNameComponent("components"))
			.withComponent(new ParentComponent(modelLookup.get(root())))
			.mergeFrom(elementsPropertyFactory.newProperty()
				.baseRef(project.getExtensions().getByType(ModelLookup.class).get(root()))
				.elementType(of(Component.class))
				.build())
			.withComponent(createdUsing(of(ComponentContainer.class), () -> new ComponentContainerAdapter(ModelNodeUtils.get(ModelNodeContext.getCurrentModelNode(), of(new TypeOf<ViewAdapter<Component>>() {})), modeRegistry)))
			.build()
		);
		project.getExtensions().add(ComponentContainer.class, "components", components.as(ComponentContainer.class).get());

		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelComponentReference.ofProjection(VariantAwareComponent.class), ModelComponentReference.of(ModelState.IsAtLeastRealized.class), (entity, projection, stateTag) -> {
			ModelStates.realize(ModelNodeUtils.getDescendant(entity, "variants"));
		}));

		model(project, objects()).configureEach(new BiConsumer<ModelObjectIdentifier, Object>() {
			@Override
			public void accept(ModelObjectIdentifier identifier, Object target) {
				if (target instanceof HasBaseName) {
					((HasBaseName) target).getBaseName().convention(project.provider(() -> {
						return model(project, objects()).parentsOf(identifier)
							.flatMap(projectionOf(HasBaseName.class))
							.map(toProviderOf(HasBaseName::getBaseName))
							.findFirst().orElseGet(() -> project.provider(notDefined()))
							.orElse(project.provider(() -> {
								if (target instanceof Named) {
									return ((Named) target).getName();
								} else {
									return null;
								}
							}));
					}).flatMap(it -> it));
				}
			}

			private /*static*/ <V> Callable<V> notDefined() {
				return () -> null;
			}

			private /*static*/ <T> Function<ModelMapAdapters.ModelElementIdentity, Stream<T>> projectionOf(Class<T> type) {
				return it -> {
					if (it.instanceOf(type)) {
						return Stream.of(it.asModelObject(type).get());
					} else {
						return Stream.empty();
					}
				};
			}

			// Useful because the intention is to use the Provider type of a Property (for example)
			private /*static*/ <T, U> Function<U, Provider<T>> toProviderOf(Function<? super U, ? extends Provider<T>> mapper) {
				return mapper::apply;
			}
		});
	}

	@SuppressWarnings("unchecked")
	private static Class<? extends Variant> variantType(ModelType<? extends VariantAwareComponent<? extends Variant>> type) {
		try {
			return (Class<? extends Variant>) ((ParameterizedType) TypeToken.of(type.getType()).resolveType(VariantAwareComponent.class.getMethod("getVariants").getGenericReturnType()).getType()).getActualTypeArguments()[0];
		} catch (NoSuchMethodException e) {
			throw new UnsupportedOperationException();
		}
	}
}
