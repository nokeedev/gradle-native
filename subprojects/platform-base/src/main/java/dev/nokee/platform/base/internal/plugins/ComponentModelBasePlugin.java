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

import com.google.common.reflect.TypeToken;
import dev.nokee.internal.Factory;
import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.internal.plugins.LanguageBasePlugin;
import dev.nokee.model.DependencyFactory;
import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.NamedDomainObjectRegistry;
import dev.nokee.model.PolymorphicDomainObjectRegistry;
import dev.nokee.model.internal.ModelPropertyIdentifier;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.core.*;
import dev.nokee.model.internal.plugins.ModelBasePlugin;
import dev.nokee.model.internal.registry.ModelConfigurer;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.model.internal.type.ModelType;
import dev.nokee.model.internal.type.TypeOf;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.ComponentContainer;
import dev.nokee.platform.base.ComponentSources;
import dev.nokee.platform.base.Variant;
import dev.nokee.platform.base.VariantAwareComponent;
import dev.nokee.platform.base.internal.*;
import dev.nokee.platform.base.internal.components.DefaultComponentContainer;
import dev.nokee.platform.base.internal.dependencies.ConsumableDependencyBucketRegistrationFactory;
import dev.nokee.platform.base.internal.dependencies.DeclarableDependencyBucketRegistrationFactory;
import dev.nokee.platform.base.internal.dependencies.DefaultDependencyBucketFactory;
import dev.nokee.platform.base.internal.dependencies.ResolvableDependencyBucketRegistrationFactory;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.provider.Provider;
import org.gradle.model.Model;

import java.lang.reflect.ParameterizedType;
import java.util.function.Consumer;

import static dev.nokee.model.internal.BaseNamedDomainObjectContainer.namedContainer;
import static dev.nokee.model.internal.BaseNamedDomainObjectView.namedView;
import static dev.nokee.model.internal.core.ModelActions.executeUsingProjection;
import static dev.nokee.model.internal.core.ModelNodes.discover;
import static dev.nokee.model.internal.core.ModelNodes.mutate;
import static dev.nokee.model.internal.core.NodePredicate.allDirectDescendants;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.platform.base.internal.LanguageSourceSetConventionSupplier.maven;
import static dev.nokee.platform.base.internal.LanguageSourceSetConventionSupplier.withConventionOf;

public class ComponentModelBasePlugin implements Plugin<Project> {
	@Override
	@SuppressWarnings("unchecked")
	public void apply(Project project) {
		project.getPluginManager().apply(ModelBasePlugin.class);
		project.getPluginManager().apply("lifecycle-base");
		project.getPluginManager().apply(LanguageBasePlugin.class);
		project.getPluginManager().apply(BinaryBasePlugin.class);
		project.getPluginManager().apply(TaskBasePlugin.class);

		val modeRegistry = project.getExtensions().getByType(ModelRegistry.class);
		val components = modeRegistry.register(components()).as(DefaultComponentContainer.class).get();
		project.getExtensions().add(ComponentContainer.class, "components", components);

		val propertyFactory = project.getExtensions().getByType(ModelPropertyRegistrationFactory.class);
		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelComponentReference.of(ModelPath.class), ModelComponentReference.of(ModelState.IsAtLeastCreated.class), ModelComponentReference.of(IsComponent.class), ModelComponentReference.ofProjection(Component.class), (e, p, ignored1, ignored2, projection) -> {
			if (ModelPath.root().isDirectDescendant(p)) {
				modeRegistry.register(propertyFactory.create(ModelPropertyIdentifier.of(ModelPropertyIdentifier.of(ProjectIdentifier.of(project),"components"), p.getName()), e));
			}
		}));

		project.getExtensions().add(ComponentVariantsPropertyRegistrationFactory.class, "__nokee_componentVariantsPropertyFactory", new ComponentVariantsPropertyRegistrationFactory(project.getExtensions().getByType(ModelRegistry.class), project.getExtensions().getByType(ModelPropertyRegistrationFactory.class), project.getProviders(), project.getExtensions().getByType(ModelLookup.class), project.getObjects()));
		project.getExtensions().add(ComponentSourcesPropertyRegistrationFactory.class, "__nokee_componentSourcesPropertyFactory", new ComponentSourcesPropertyRegistrationFactory(project.getExtensions().getByType(ModelRegistry.class), project.getExtensions().getByType(ModelPropertyRegistrationFactory.class), project.getExtensions().getByType(ModelConfigurer.class), project.getProviders(), project.getExtensions().getByType(ModelLookup.class), project.getObjects()));
		project.getExtensions().add(ComponentDependenciesPropertyRegistrationFactory.class, "__nokee_componentDependenciesPropertyFactory", new ComponentDependenciesPropertyRegistrationFactory(project.getExtensions().getByType(ModelRegistry.class), project.getExtensions().getByType(ModelPropertyRegistrationFactory.class), project.getExtensions().getByType(ModelConfigurer.class), project.getExtensions().getByType(ModelLookup.class)));
		project.getExtensions().add(ComponentBinariesPropertyRegistrationFactory.class, "__nokee_componentBinariesPropertyFactory", new ComponentBinariesPropertyRegistrationFactory(project.getExtensions().getByType(ModelRegistry.class), project.getExtensions().getByType(ModelPropertyRegistrationFactory.class), project.getExtensions().getByType(ModelConfigurer.class), project.getProviders(), project.getExtensions().getByType(ModelLookup.class), project.getObjects()));
		project.getExtensions().add(ComponentTasksPropertyRegistrationFactory.class, "__nokee_componentTasksPropertyFactory", new ComponentTasksPropertyRegistrationFactory(project.getExtensions().getByType(ModelRegistry.class), project.getExtensions().getByType(ModelPropertyRegistrationFactory.class), project.getExtensions().getByType(ModelConfigurer.class), project.getProviders(), project.getExtensions().getByType(ModelLookup.class), project.getObjects()));

		project.getExtensions().add("__nokee_declarableBucketFactory", new DeclarableDependencyBucketRegistrationFactory(NamedDomainObjectRegistry.of(project.getConfigurations()), new DefaultDependencyBucketFactory(NamedDomainObjectRegistry.of(project.getConfigurations()), DependencyFactory.forProject(project))));
		project.getExtensions().add("__nokee_resolvableBucketFactory", new ResolvableDependencyBucketRegistrationFactory(NamedDomainObjectRegistry.of(project.getConfigurations()), new DefaultDependencyBucketFactory(NamedDomainObjectRegistry.of(project.getConfigurations()), DependencyFactory.forProject(project))));
		project.getExtensions().add("__nokee_consumableBucketFactory", new ConsumableDependencyBucketRegistrationFactory(NamedDomainObjectRegistry.of(project.getConfigurations()), new DefaultDependencyBucketFactory(NamedDomainObjectRegistry.of(project.getConfigurations()), DependencyFactory.forProject(project)), project.getObjects()));

		project.getExtensions().add(DimensionPropertyRegistrationFactory.class, "__nokee_dimensionPropertyFactory", new DimensionPropertyRegistrationFactory(project.getObjects()));
		project.getExtensions().add(TaskRegistrationFactory.class, "__nokee_taskRegistrationFactory", new TaskRegistrationFactory(PolymorphicDomainObjectRegistry.of(project.getTasks()), TaskNamer.INSTANCE));

		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelComponentReference.of(ModelState.IsAtLeastRegistered.class), ModelComponentReference.ofProjection(ModelType.of(new TypeOf<ModelBackedVariantAwareComponentMixIn<? extends Variant>>() {})), ModelComponentReference.ofAny(ModelComponentType.componentOf(DomainObjectIdentifier.class)), (entity, ignored, component, identifier) -> {
			val registry = project.getExtensions().getByType(ModelRegistry.class);
			val dimensions = project.getExtensions().getByType(DimensionPropertyRegistrationFactory.class);
			val buildVariants = entity.addComponent(new BuildVariants(entity, project.getProviders(), project.getObjects()));
			entity.addComponent(new ModelBackedVariantDimensions(identifier, registry, dimensions));

			val bv = registry.register(dimensions.buildVariants(ModelPropertyIdentifier.of(identifier, "buildVariants"), buildVariants.get()));
			entity.addComponent(new BuildVariantsPropertyComponent(ModelNodes.of(bv)));

			registry.register(project.getExtensions().getByType(ComponentVariantsPropertyRegistrationFactory.class).create(ModelPropertyIdentifier.of(identifier, "variants"), variantType((ModelType<VariantAwareComponent<? extends Variant>>) component.getType())));
		}));
		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelComponentReference.of(ModelState.IsAtLeastFinalized.class), ModelComponentReference.of(BuildVariantsPropertyComponent.class), (entity, ignored, buildVariants) -> {
			// TODO: Each plugins should just map the build variants into the variants.
			((Provider<?>) buildVariants.get().getComponent(GradlePropertyComponent.class).get()).get();
		}));
	}

	@SuppressWarnings("unchecked")
	private static Class<? extends Variant> variantType(ModelType<? extends VariantAwareComponent<? extends Variant>> type) {
		try {
			return (Class<? extends Variant>) ((ParameterizedType) TypeToken.of(type.getType()).resolveType(VariantAwareComponent.class.getMethod("getVariants").getGenericReturnType()).getType()).getActualTypeArguments()[0];
		} catch (NoSuchMethodException e) {
			throw new UnsupportedOperationException();
		}
	}

	private static NodeRegistration components() {
		return namedContainer("components", of(DefaultComponentContainer.class));
	}

	public static <T extends Component> NodeRegistration component(String name, Class<T> type) {
		return NodeRegistration.of(name, of(type))
			.action(configureSourceSetConventionUsingMavenLayout(ComponentName.of(name)));
	}

	public static <T extends Component> NodeRegistration component(String name, Class<T> type, Factory<T> factory) {
		return NodeRegistration.unmanaged(name, of(type), factory)
			.action(configureSourceSetConventionUsingMavenLayout(ComponentName.of(name)));
	}

	private static NodeAction configureSourceSetConventionUsingMavenLayout(ComponentName componentName) {
		return whenComponentSourcesDiscovered().apply(configureEachSourceSet(of(LanguageSourceSet.class), withConventionOf(maven(componentName))));
	}

	public static <T extends LanguageSourceSet> ModelAction configureEachSourceSet(ModelType<T> type, Consumer<? super T> action) {
		return ModelActionWithInputs.of(ModelComponentReference.of(RelativeConfigurationService.class), (node, configure) -> {
			assert ModelNodeUtils.canBeViewedAs(node, of(ComponentSources.class)) : "should only apply to ComponentSources";
			ModelNodeUtils.applyTo(node, allDirectDescendants(mutate(type)).apply(executeUsingProjection(type, action::accept)));
		});
	}

	public static NodePredicate whenComponentSourcesDiscovered() {
		return allDirectDescendants(discover(of(ComponentSources.class)));
	}

	public static <T extends ComponentSources> NodeRegistration componentSourcesOf(Class<T> sourcesType) {
		return namedView("sources", of(sourcesType));
	}
}
