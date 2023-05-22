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
import com.google.common.collect.MoreCollectors;
import com.google.common.reflect.TypeToken;
import dev.nokee.model.capabilities.variants.CreateVariantsRule;
import dev.nokee.model.capabilities.variants.KnownVariantInformationElement;
import dev.nokee.model.internal.DefaultDomainObjectIdentifier;
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
import dev.nokee.model.internal.core.ModelPropertyRegistrationFactory;
import dev.nokee.model.internal.core.ParentComponent;
import dev.nokee.model.internal.core.ParentUtils;
import dev.nokee.model.internal.names.ElementNameComponent;
import dev.nokee.model.internal.plugins.ModelBasePlugin;
import dev.nokee.model.internal.registry.ModelConfigurer;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.model.internal.tags.ModelComponentTag;
import dev.nokee.model.internal.type.ModelType;
import dev.nokee.model.internal.type.TypeOf;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryAwareComponent;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.platform.base.BuildVariant;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.ComponentContainer;
import dev.nokee.platform.base.ComponentDependencies;
import dev.nokee.platform.base.DependencyAwareComponent;
import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.base.SourceAwareComponent;
import dev.nokee.platform.base.TaskAwareComponent;
import dev.nokee.platform.base.TaskView;
import dev.nokee.platform.base.Variant;
import dev.nokee.platform.base.VariantAwareComponent;
import dev.nokee.platform.base.VariantView;
import dev.nokee.platform.base.internal.BaseNameComponent;
import dev.nokee.platform.base.internal.BaseNamePropertyComponent;
import dev.nokee.platform.base.internal.BinaryViewAdapter;
import dev.nokee.platform.base.internal.BuildVariants;
import dev.nokee.platform.base.internal.BuildVariantsPropertyComponent;
import dev.nokee.platform.base.internal.ComponentContainerAdapter;
import dev.nokee.platform.base.internal.ComponentTasksPropertyRegistrationFactory;
import dev.nokee.platform.base.internal.DimensionPropertyRegistrationFactory;
import dev.nokee.platform.base.internal.IsTask;
import dev.nokee.platform.base.internal.ModelBackedBinaryAwareComponentMixIn;
import dev.nokee.platform.base.internal.ModelBackedDependencyAwareComponentMixIn;
import dev.nokee.platform.base.internal.ModelBackedHasBaseNameMixIn;
import dev.nokee.platform.base.internal.ModelBackedTaskAwareComponentMixIn;
import dev.nokee.platform.base.internal.ModelBackedVariantAwareComponentMixIn;
import dev.nokee.platform.base.internal.ModelBackedVariantDimensions;
import dev.nokee.platform.base.internal.TaskViewAdapter;
import dev.nokee.platform.base.internal.VariantViewAdapter;
import dev.nokee.platform.base.internal.ViewAdapter;
import dev.nokee.platform.base.internal.assembletask.AssembleTaskCapabilityPlugin;
import dev.nokee.platform.base.internal.dependencies.DependencyBucketCapabilityPlugin;
import dev.nokee.platform.base.internal.developmentbinary.DevelopmentBinaryCapability;
import dev.nokee.platform.base.internal.developmentvariant.DevelopmentVariantCapability;
import dev.nokee.platform.base.internal.elements.ComponentElementsCapabilityPlugin;
import dev.nokee.platform.base.internal.elements.ComponentElementsPropertyRegistrationFactory;
import dev.nokee.platform.base.internal.extensionaware.ExtensionAwareCapability;
import dev.nokee.platform.base.internal.project.ProjectCapabilityPlugin;
import dev.nokee.platform.base.internal.project.ProjectProjectionComponent;
import dev.nokee.platform.base.internal.tasks.TaskCapabilityPlugin;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.Set;
import java.util.function.Supplier;

import static com.google.common.base.Suppliers.ofInstance;
import static dev.nokee.model.internal.core.ModelPath.root;
import static dev.nokee.model.internal.core.ModelProjections.createdUsing;
import static dev.nokee.model.internal.core.ModelRegistration.builder;
import static dev.nokee.model.internal.type.ModelType.of;

public class ComponentModelBasePlugin implements Plugin<Project> {
	@Override
	@SuppressWarnings("unchecked")
	public void apply(Project project) {
		project.getPluginManager().apply(ModelBasePlugin.class);
		project.getPluginManager().apply("lifecycle-base");

		val modeRegistry = project.getExtensions().getByType(ModelRegistry.class);
		val modelLookup = project.getExtensions().getByType(ModelLookup.class);

		project.getExtensions().getByType(ModelLookup.class).get(root()).addComponent(new ProjectProjectionComponent(project));

		project.getExtensions().add(ComponentTasksPropertyRegistrationFactory.class, "__nokee_componentTasksPropertyFactory", new ComponentTasksPropertyRegistrationFactory());

		project.getPluginManager().apply(DependencyBucketCapabilityPlugin.class);
		project.getPluginManager().apply(TaskCapabilityPlugin.class);
		project.getPluginManager().apply(ProjectCapabilityPlugin.class);
		project.getPluginManager().apply(AssembleTaskCapabilityPlugin.class);

		project.getExtensions().add(DimensionPropertyRegistrationFactory.class, "__nokee_dimensionPropertyFactory", new DimensionPropertyRegistrationFactory(project.getObjects()));

		project.getPluginManager().apply(DevelopmentBinaryCapability.class);
		project.getPluginManager().apply(DevelopmentVariantCapability.class);
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
		project.getExtensions().getByType(ModelConfigurer.class).configure(new ModelActionWithInputs.ModelAction2<ModelState.IsAtLeastFinalized, BuildVariantsPropertyComponent>() {
			protected void execute(ModelNode entity, ModelState.IsAtLeastFinalized ignored, BuildVariantsPropertyComponent buildVariants) {
				// TODO: Each plugins should just map the build variants into the variants.
				//   Sort-of, each plugins should complete the configuration but not create the variant themselves
				final ImmutableSet.Builder<KnownVariantInformationElement> builder = ImmutableSet.builder();
				((Provider<Set<BuildVariant>>) buildVariants.get().get(GradlePropertyComponent.class).get()).get().forEach(it -> {
					builder.add(new KnownVariantInformationElement(it.toString()));
				});
				entity.addComponent(ModelBuffers.of(KnownVariantInformationElement.class, builder.build()));
			}
		});
		project.getExtensions().getByType(ModelConfigurer.class).configure(new OnDiscover(ModelActionWithInputs.of(ModelComponentReference.ofProjection(ModelType.of(ModelBackedTaskAwareComponentMixIn.class)), ModelComponentReference.of(IdentifierComponent.class), (entity, projection, identifier) -> {
			modeRegistry.register(builder()
				.withComponent(new ElementNameComponent("tasks"))
				.withComponent(new ParentComponent(entity))
				.mergeFrom(elementsPropertyFactory.newProperty().baseRef(entity).elementType(of(Task.class)).build())
				.withComponent(createdUsing(of(TaskView.class), () -> new TaskViewAdapter<>(ModelNodeUtils.get(ModelNodeContext.getCurrentModelNode(), ModelType.of(new TypeOf<ViewAdapter<Task>>() {})))))
				.build());
		})));
		project.getExtensions().getByType(ModelConfigurer.class).configure(new OnDiscover(ModelActionWithInputs.of(ModelComponentReference.ofProjection(ModelType.of(ModelBackedBinaryAwareComponentMixIn.class)), ModelComponentReference.of(IdentifierComponent.class), (entity, projection, identifier) -> {
			modeRegistry.register(builder()
				.withComponent(new ElementNameComponent("binaries"))
				.withComponent(new ParentComponent(entity))
				.mergeFrom(elementsPropertyFactory.newProperty().baseRef(entity).elementType(of(Binary.class)).build())
				.withComponent(createdUsing(of(BinaryView.class), () -> new BinaryViewAdapter<>(ModelNodeUtils.get(ModelNodeContext.getCurrentModelNode(), ModelType.of(new TypeOf<ViewAdapter<Binary>>() {})))))
				.build());
		})));
		project.getExtensions().getByType(ModelConfigurer.class).configure(new OnDiscover(ModelActionWithInputs.of(ModelComponentReference.ofProjection(ModelType.of(new TypeOf<ModelBackedDependencyAwareComponentMixIn<? extends ComponentDependencies, ? extends ComponentDependencies>>() {})), (entity, projection) -> {
			Class<ComponentDependencies> type = (Class<ComponentDependencies>) dependenciesType((ModelType<DependencyAwareComponent<? extends ComponentDependencies>>)projection.getType());
			modeRegistry.register(builder()
				.withComponent(new ElementNameComponent("dependencies"))
				.withComponent(new ParentComponent(entity))
				.mergeFrom(elementsPropertyFactory.newProperty().baseRef(entity).elementType(of(DependencyBucket.class)).build())
				.withComponent(createdUsing(of(type), () -> {
					try {
						return type.getConstructor().newInstance();
					} catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
						throw new RuntimeException(e);
					}
				}))
				.build());
		})));

		project.getExtensions().getByType(ModelConfigurer.class).configure(new ModelActionWithInputs.ModelAction5<ModelComponentTag<IsTask>, ModelPathComponent, DisplayNameComponent, ElementNameComponent, ModelState.IsAtLeastCreated>() {
			// ComponentFromEntity<ParentComponent> read-only self
			protected void execute(ModelNode entity, ModelComponentTag<IsTask> ignored1, ModelPathComponent path, DisplayNameComponent displayName, ElementNameComponent elementName, ModelState.IsAtLeastCreated ignored2) {
				if (!entity.has(IdentifierComponent.class)) {
					val parentIdentifier = entity.find(ParentComponent.class).map(parent -> parent.get().get(IdentifierComponent.class).get()).orElse(null);
					entity.addComponent(new IdentifierComponent(new DefaultDomainObjectIdentifier(elementName.get(), parentIdentifier, displayName.get(), path.get())));
				}
			}
		});

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

		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelComponentReference.ofProjection(BinaryAwareComponent.class), ModelComponentReference.of(ModelState.IsAtLeastRealized.class), (entity, projection, stateTag) -> {
			ModelStates.realize(ModelNodeUtils.getDescendant(entity, "binaries"));
		}));
		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelComponentReference.ofProjection(SourceAwareComponent.class), ModelComponentReference.of(ModelState.IsAtLeastRealized.class), (entity, projection, stateTag) -> {
			ModelStates.realize(ModelNodeUtils.getDescendant(entity, "sources"));
		}));
		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelComponentReference.ofProjection(DependencyAwareComponent.class), ModelComponentReference.of(ModelState.IsAtLeastRealized.class), (entity, projection, stateTag) -> {
			ModelStates.realize(ModelNodeUtils.getDescendant(entity, "dependencies"));
		}));
		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelComponentReference.ofProjection(TaskAwareComponent.class), ModelComponentReference.of(ModelState.IsAtLeastRealized.class), (entity, projection, stateTag) -> {
			ModelStates.realize(ModelNodeUtils.getDescendant(entity, "tasks"));
		}));
		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelComponentReference.ofProjection(VariantAwareComponent.class), ModelComponentReference.of(ModelState.IsAtLeastRealized.class), (entity, projection, stateTag) -> {
			ModelStates.realize(ModelNodeUtils.getDescendant(entity, "variants"));
		}));

		project.getExtensions().getByType(ModelConfigurer.class).configure(new OnDiscover(ModelActionWithInputs.of(ModelComponentReference.ofProjection(ModelBackedHasBaseNameMixIn.class), (entity, projection) -> {
			val baseNameProperty = modeRegistry.instantiate(builder().withComponent(new ElementNameComponent("baseName")).withComponent(new ParentComponent(entity)).mergeFrom(ModelPropertyRegistrationFactory.property(String.class)).build());
			ModelStates.register(baseNameProperty);
			entity.addComponent(new BaseNamePropertyComponent(baseNameProperty));
		})));

		project.getExtensions().getByType(ModelConfigurer.class).configure(new ModelActionWithInputs.ModelAction1<BaseNamePropertyComponent>() {
			protected void execute(ModelNode entity, BaseNamePropertyComponent property) {
				((Property<String>) property.get().get(GradlePropertyComponent.class).get()).convention(project.getProviders().provider(() -> {
					return entity.find(ParentComponent.class)
						.flatMap(parent -> ParentUtils.stream(parent).map(ModelStates::finalize).filter(it -> it.has(BaseNameComponent.class)).findFirst())
						.map(parent -> (Supplier<String>) parent.get(BaseNameComponent.class)::get)
						.orElseGet(() -> entity.find(ElementNameComponent.class).map(it -> (Supplier<String>) it.get()::toString).orElse(ofInstance(null)))
						.get();
				}));
			}
		});

		project.getExtensions().getByType(ModelConfigurer.class).configure(new ModelActionWithInputs.ModelAction2<BaseNamePropertyComponent, ModelStates.Finalizing>() {
			// ComponentFromEntity<GradlePropertyComponent> read-write on BaseNamePropertyComponent
			protected void execute(ModelNode entity, BaseNamePropertyComponent property, ModelStates.Finalizing ignored1) {
				entity.addComponent(new BaseNameComponent(((Property<String>) property.get().get(GradlePropertyComponent.class).get()).get()));
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

	@SuppressWarnings("unchecked")
	private static Class<? extends ComponentDependencies> dependenciesType(ModelType<? extends DependencyAwareComponent<? extends ComponentDependencies>> type) {
		val t = type.getInterfaces().stream().filter(it -> it.getRawType().equals(ModelBackedDependencyAwareComponentMixIn.class)).map(it -> (ModelType<ComponentDependencies>) it).collect(MoreCollectors.onlyElement());
		return (Class<? extends ComponentDependencies>) ((ParameterizedType) t.getType()).getActualTypeArguments()[1];
	}
}
