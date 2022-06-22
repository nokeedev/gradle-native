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

import com.google.common.collect.MoreCollectors;
import com.google.common.reflect.TypeToken;
import dev.nokee.model.DependencyFactory;
import dev.nokee.model.NamedDomainObjectRegistry;
import dev.nokee.model.PolymorphicDomainObjectRegistry;
import dev.nokee.model.internal.ModelPropertyIdentifier;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.core.GradlePropertyComponent;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelComponentReference;
import dev.nokee.model.internal.core.ModelNodeContext;
import dev.nokee.model.internal.core.ModelNodeUtils;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.core.ModelPath;
import dev.nokee.model.internal.core.ModelPropertyRegistrationFactory;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.core.ParentComponent;
import dev.nokee.model.internal.names.ElementNameComponent;
import dev.nokee.model.internal.names.FullyQualifiedNameComponent;
import dev.nokee.model.internal.names.NamingScheme;
import dev.nokee.model.internal.names.NamingSchemeSystem;
import dev.nokee.model.internal.plugins.ModelBasePlugin;
import dev.nokee.model.internal.registry.ModelConfigurer;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.model.internal.type.ModelType;
import dev.nokee.model.internal.type.TypeOf;
import dev.nokee.platform.base.Artifact;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryAwareComponent;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.ComponentContainer;
import dev.nokee.platform.base.ComponentDependencies;
import dev.nokee.platform.base.DependencyAwareComponent;
import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.base.HasDevelopmentVariant;
import dev.nokee.platform.base.SourceAwareComponent;
import dev.nokee.platform.base.TaskAwareComponent;
import dev.nokee.platform.base.TaskView;
import dev.nokee.platform.base.Variant;
import dev.nokee.platform.base.VariantAwareComponent;
import dev.nokee.platform.base.VariantView;
import dev.nokee.platform.base.internal.BaseNamePropertyComponent;
import dev.nokee.platform.base.internal.BinaryViewAdapter;
import dev.nokee.platform.base.internal.BuildVariants;
import dev.nokee.platform.base.internal.BuildVariantsPropertyComponent;
import dev.nokee.platform.base.internal.ComponentContainerAdapter;
import dev.nokee.platform.base.internal.ComponentDependenciesProperty;
import dev.nokee.platform.base.internal.ComponentVariantsProperty;
import dev.nokee.platform.base.internal.DevelopmentVariantProperty;
import dev.nokee.platform.base.internal.DimensionPropertyRegistrationFactory;
import dev.nokee.platform.base.internal.IsBinary;
import dev.nokee.platform.base.internal.IsComponent;
import dev.nokee.platform.base.internal.IsDependencyBucket;
import dev.nokee.platform.base.internal.IsTask;
import dev.nokee.platform.base.internal.IsVariant;
import dev.nokee.platform.base.internal.ModelBackedBinaryAwareComponentMixIn;
import dev.nokee.platform.base.internal.ModelBackedDependencyAwareComponentMixIn;
import dev.nokee.platform.base.internal.ModelBackedHasBaseNameMixIn;
import dev.nokee.platform.base.internal.ModelBackedHasDevelopmentVariantMixIn;
import dev.nokee.platform.base.internal.ModelBackedTaskAwareComponentMixIn;
import dev.nokee.platform.base.internal.ModelBackedVariantAwareComponentMixIn;
import dev.nokee.platform.base.internal.ModelBackedVariantDimensions;
import dev.nokee.platform.base.internal.RegisterAssembleLifecycleTaskRule;
import dev.nokee.platform.base.internal.TaskNamer;
import dev.nokee.platform.base.internal.TaskRegistrationFactory;
import dev.nokee.platform.base.internal.TaskViewAdapter;
import dev.nokee.platform.base.internal.VariantViewAdapter;
import dev.nokee.platform.base.internal.ViewAdapter;
import dev.nokee.platform.base.internal.dependencies.ConsumableDependencyBucketRegistrationFactory;
import dev.nokee.platform.base.internal.dependencies.DeclarableDependencyBucketRegistrationFactory;
import dev.nokee.platform.base.internal.dependencies.DefaultDependencyBucketFactory;
import dev.nokee.platform.base.internal.dependencies.ResolvableDependencyBucketRegistrationFactory;
import dev.nokee.platform.base.internal.elements.ComponentElementsCapabilityPlugin;
import dev.nokee.platform.base.internal.elements.ComponentElementsPropertyRegistrationFactory;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.internal.artifacts.configurations.ConfigurationInternal;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.Collections;
import java.util.Objects;

import static dev.nokee.model.internal.core.ModelProjections.createdUsing;
import static dev.nokee.model.internal.tags.ModelTags.typeOf;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.utils.ProviderUtils.finalizeValueOnRead;

public class ComponentModelBasePlugin implements Plugin<Project> {
	@Override
	@SuppressWarnings("unchecked")
	public void apply(Project project) {
		project.getPluginManager().apply(ModelBasePlugin.class);
		project.getPluginManager().apply("lifecycle-base");

		val modeRegistry = project.getExtensions().getByType(ModelRegistry.class);

		project.getExtensions().add("__nokee_declarableBucketFactory", new DeclarableDependencyBucketRegistrationFactory(NamedDomainObjectRegistry.of(project.getConfigurations()), new DefaultDependencyBucketFactory(NamedDomainObjectRegistry.of(project.getConfigurations()), DependencyFactory.forProject(project))));
		project.getExtensions().add("__nokee_resolvableBucketFactory", new ResolvableDependencyBucketRegistrationFactory(NamedDomainObjectRegistry.of(project.getConfigurations()), new DefaultDependencyBucketFactory(NamedDomainObjectRegistry.of(project.getConfigurations()), DependencyFactory.forProject(project))));
		project.getExtensions().add("__nokee_consumableBucketFactory", new ConsumableDependencyBucketRegistrationFactory(NamedDomainObjectRegistry.of(project.getConfigurations()), new DefaultDependencyBucketFactory(NamedDomainObjectRegistry.of(project.getConfigurations()), DependencyFactory.forProject(project)), project.getObjects()));

		project.getConfigurations().configureEach(configuration -> {
			final Provider<Object> g = finalizeValueOnRead(project.getObjects().property(Object.class).value(project.provider(() -> {
				project.getExtensions().getByType(ModelLookup.class).query(entity -> entity.hasComponent(typeOf(IsDependencyBucket.class)) && entity.find(FullyQualifiedNameComponent.class).map(FullyQualifiedNameComponent::get).map(Objects::toString).map(configuration.getName()::equals).orElse(false)).forEach(ModelStates::realize);
				return null;
			})));
			configuration.defaultDependencies(it -> {
				g.getOrNull();
			});
			((ConfigurationInternal) configuration).beforeLocking(it -> {
				g.getOrNull();
			});
		});
		project.getTasks().configureEach(task -> {
			project.getExtensions().getByType(ModelLookup.class).query(entity -> entity.hasComponent(typeOf(IsTask.class)) && entity.find(FullyQualifiedNameComponent.class).map(FullyQualifiedNameComponent::get).map(Objects::toString).map(task.getName()::equals).orElse(false)).forEach(ModelStates::realize);
		});

		project.getExtensions().add(DimensionPropertyRegistrationFactory.class, "__nokee_dimensionPropertyFactory", new DimensionPropertyRegistrationFactory(project.getObjects()));
		project.getExtensions().add(TaskRegistrationFactory.class, "__nokee_taskRegistrationFactory", new TaskRegistrationFactory(project.getTasks(), PolymorphicDomainObjectRegistry.of(project.getTasks()), TaskNamer.INSTANCE));

		val elementsPropertyFactory = new ComponentElementsPropertyRegistrationFactory();
		project.getExtensions().getByType(ModelConfigurer.class).configure(new OnDiscover(ModelActionWithInputs.of(ModelComponentReference.ofProjection(ModelType.of(new TypeOf<ModelBackedVariantAwareComponentMixIn<? extends Variant>>() {})), ModelComponentReference.of(IdentifierComponent.class), ModelComponentReference.of(ParentComponent.class), (entity, component, identifier, parent) -> {
			val registry = project.getExtensions().getByType(ModelRegistry.class);
			val dimensions = project.getExtensions().getByType(DimensionPropertyRegistrationFactory.class);
			val buildVariants = entity.addComponent(new BuildVariants(entity, project.getProviders(), project.getObjects()));
			entity.addComponent(new ModelBackedVariantDimensions(identifier.get(), registry, dimensions));

			val bv = registry.register(dimensions.buildVariants(ModelPropertyIdentifier.of(identifier.get(), "buildVariants"), buildVariants.get()));
			entity.addComponent(new BuildVariantsPropertyComponent(ModelNodes.of(bv)));

			val variantsProperty = registry.register(ModelRegistration.builder()
				.withComponent(new IdentifierComponent(ModelPropertyIdentifier.of(identifier.get(), "variants")))
				.mergeFrom(elementsPropertyFactory.newProperty().baseRef(parent.get()).elementType(of(variantType((ModelType<VariantAwareComponent<? extends Variant>>) component.getType()))).build())
				.withComponent(createdUsing(of(VariantView.class), () -> new VariantViewAdapter<>(ModelNodeUtils.get(ModelNodeContext.getCurrentModelNode(), of(new TypeOf<ViewAdapter<? extends Variant>>() {})))))
				.build());
			entity.addComponent(new ComponentVariantsProperty(ModelNodes.of(variantsProperty)));
		})));
		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelComponentReference.of(ModelState.IsAtLeastFinalized.class), ModelComponentReference.of(BuildVariantsPropertyComponent.class), (entity, ignored, buildVariants) -> {
			// TODO: Each plugins should just map the build variants into the variants.
			((Provider<?>) buildVariants.get().get(GradlePropertyComponent.class).get()).get();
		}));
		project.getExtensions().getByType(ModelConfigurer.class).configure(new OnDiscover(ModelActionWithInputs.of(ModelComponentReference.ofProjection(ModelType.of(ModelBackedTaskAwareComponentMixIn.class)), ModelComponentReference.of(IdentifierComponent.class), (entity, projection, identifier) -> {
			modeRegistry.register(ModelRegistration.builder()
				.withComponent(new IdentifierComponent(ModelPropertyIdentifier.of(identifier.get(), "tasks")))
				.mergeFrom(elementsPropertyFactory.newProperty().baseRef(entity).elementType(of(Task.class)).build())
				.withComponent(createdUsing(of(TaskView.class), () -> new TaskViewAdapter<>(ModelNodeUtils.get(ModelNodeContext.getCurrentModelNode(), ModelType.of(new TypeOf<ViewAdapter<Task>>() {})))))
				.build());
		})));
		project.getExtensions().getByType(ModelConfigurer.class).configure(new OnDiscover(ModelActionWithInputs.of(ModelComponentReference.ofProjection(ModelType.of(ModelBackedBinaryAwareComponentMixIn.class)), ModelComponentReference.of(IdentifierComponent.class), (entity, projection, identifier) -> {
			modeRegistry.register(ModelRegistration.builder()
				.withComponent(new IdentifierComponent(ModelPropertyIdentifier.of(identifier.get(), "binaries")))
				.mergeFrom(elementsPropertyFactory.newProperty().baseRef(entity).elementType(of(Binary.class)).build())
				.withComponent(createdUsing(of(BinaryView.class), () -> new BinaryViewAdapter<>(ModelNodeUtils.get(ModelNodeContext.getCurrentModelNode(), ModelType.of(new TypeOf<ViewAdapter<Binary>>() {})))))
				.build());
		})));
		project.getExtensions().getByType(ModelConfigurer.class).configure(new OnDiscover(ModelActionWithInputs.of(ModelComponentReference.ofProjection(ModelType.of(new TypeOf<ModelBackedDependencyAwareComponentMixIn<? extends ComponentDependencies, ? extends ComponentDependencies>>() {})), ModelComponentReference.of(IdentifierComponent.class), (entity, projection, identifier) -> {
			Class<ComponentDependencies> type = (Class<ComponentDependencies>) dependenciesType((ModelType<DependencyAwareComponent<? extends ComponentDependencies>>)projection.getType());
			val dependenciesProperty = modeRegistry.register(ModelRegistration.builder()
				.withComponent(new IdentifierComponent(ModelPropertyIdentifier.of(identifier.get(), "dependencies")))
				.mergeFrom(elementsPropertyFactory.newProperty().baseRef(entity).elementType(of(DependencyBucket.class)).build())
				.withComponent(createdUsing(of(type), () -> {
					try {
						return type.getConstructor().newInstance();
					} catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
						throw new RuntimeException(e);
					}
				}))
				.build());
			entity.addComponent(new ComponentDependenciesProperty(ModelNodes.of(dependenciesProperty)));
		})));

		project.getExtensions().getByType(ModelConfigurer.class).configure(new NamingSchemeSystem<>(IsBinary.class, NamingScheme::prefixTo));
		project.getExtensions().getByType(ModelConfigurer.class).configure(new NamingSchemeSystem<>(IsTask.class, NamingScheme::suffixTo));
		project.getExtensions().getByType(ModelConfigurer.class).configure(new NamingSchemeSystem<>(IsComponent.class, NamingScheme::prefixTo));
		project.getExtensions().getByType(ModelConfigurer.class).configure(new NamingSchemeSystem<>(IsDependencyBucket.class, NamingScheme::prefixTo));
		project.getExtensions().getByType(ModelConfigurer.class).configure(new NamingSchemeSystem<>(IsVariant.class, NamingScheme::prefixTo));

		project.getPluginManager().apply(ComponentElementsCapabilityPlugin.class);

		val components = modeRegistry.register(ModelRegistration.builder()
			.withComponent(new IdentifierComponent(ModelPropertyIdentifier.of(ProjectIdentifier.of(project), "components")))
			.mergeFrom(elementsPropertyFactory.newProperty()
				.baseRef(project.getExtensions().getByType(ModelLookup.class).get(ModelPath.root()))
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

		project.getExtensions().getByType(ModelConfigurer.class).configure(new OnDiscover(ModelActionWithInputs.of(ModelComponentReference.ofProjection(ModelBackedHasBaseNameMixIn.class), ModelComponentReference.of(IdentifierComponent.class), (entity, projection, identifier) -> {
			val baseNameProperty = modeRegistry.instantiate(project.getExtensions().getByType(ModelPropertyRegistrationFactory.class).createProperty(ModelPropertyIdentifier.of(identifier.get(), "baseName"), String.class));
			ModelStates.register(baseNameProperty);
			entity.addComponent(new BaseNamePropertyComponent(baseNameProperty));
		})));

		// ComponentFromEntity<GradlePropertyComponent> on BaseNamePropertyComponent
		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelComponentReference.of(BaseNamePropertyComponent.class), (entity, baseName) -> {
			((Property<String>) baseName.get().get(GradlePropertyComponent.class).get()).convention(project.provider(() -> {
				return entity.find(ParentComponent.class).map(ParentComponent::get)
					.flatMap(it -> it.find(BaseNamePropertyComponent.class))
					.map(it -> ((Provider<String>) it.get().get(GradlePropertyComponent.class).get()).getOrNull())
					.orElseGet(() -> entity.find(ElementNameComponent.class).map(it -> it.get().toString()).orElse(null));
			}));
		}));

		project.getExtensions().getByType(ModelConfigurer.class).configure(new OnDiscover(new RegisterAssembleLifecycleTaskRule(project.getExtensions().getByType(TaskRegistrationFactory.class), project.getExtensions().getByType(ModelRegistry.class))));

		project.getExtensions().getByType(ModelConfigurer.class).configure(new OnDiscover(ModelActionWithInputs.of(ModelComponentReference.ofProjection(ModelBackedHasDevelopmentVariantMixIn.class), ModelComponentReference.of(IdentifierComponent.class), (entity, tag, identifier) -> {
			val developmentVariantProperty = modeRegistry.register(project.getExtensions().getByType(ModelPropertyRegistrationFactory.class).createProperty(ModelPropertyIdentifier.of(identifier.get(), "developmentVariant"), developmentVariantType((ModelType<? extends HasDevelopmentVariant<? extends Variant>>) tag.getType())));
			entity.addComponent(new DevelopmentVariantProperty(ModelNodes.of(developmentVariantProperty)));
		})));
	}

	@SuppressWarnings("unchecked")
	private static Class<? extends Variant> developmentVariantType(ModelType<? extends HasDevelopmentVariant<? extends Variant>> type) {
		val t = type.getInterfaces().stream().filter(it -> it.getRawType().equals(ModelBackedHasDevelopmentVariantMixIn.class)).map(it -> (ModelType<ModelBackedHasDevelopmentVariantMixIn<?>>) it).collect(MoreCollectors.onlyElement());
		return (Class<? extends Variant>) ((ParameterizedType) t.getType()).getActualTypeArguments()[0];
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
