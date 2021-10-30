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

import com.google.common.collect.Streams;
import dev.nokee.internal.Factory;
import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.internal.plugins.LanguageBasePlugin;
import dev.nokee.model.DependencyFactory;
import dev.nokee.model.HasName;
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
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.ComponentContainer;
import dev.nokee.platform.base.ComponentSources;
import dev.nokee.platform.base.internal.*;
import dev.nokee.platform.base.internal.components.DefaultComponentContainer;
import dev.nokee.platform.base.internal.dependencies.ConsumableDependencyBucketRegistrationFactory;
import dev.nokee.platform.base.internal.dependencies.DeclarableDependencyBucketRegistrationFactory;
import dev.nokee.platform.base.internal.dependencies.DefaultDependencyBucketFactory;
import dev.nokee.platform.base.internal.dependencies.ResolvableDependencyBucketRegistrationFactory;
import dev.nokee.platform.base.internal.tasks.TaskIdentifier;
import dev.nokee.platform.base.internal.tasks.TaskName;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
	public void apply(Project project) {
		project.getPluginManager().apply(ModelBasePlugin.class);
		project.getPluginManager().apply("lifecycle-base");
		project.getPluginManager().apply(LanguageBasePlugin.class);
		project.getPluginManager().apply(BinaryBasePlugin.class);
		project.getPluginManager().apply(TaskBasePlugin.class);
		project.getPluginManager().apply(VariantBasePlugin.class);

		val modeRegistry = project.getExtensions().getByType(ModelRegistry.class);
		val components = modeRegistry.register(components()).as(DefaultComponentContainer.class).get();
		project.getExtensions().add(ComponentContainer.class, "components", components);

		val propertyFactory = project.getExtensions().getByType(ModelPropertyRegistrationFactory.class);
		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelComponentReference.of(ModelPath.class), ModelComponentReference.of(ModelState.IsAtLeastCreated.class), ModelComponentReference.of(IsComponent.class), ModelComponentReference.ofAny(ModelComponentType.projectionOf(Component.class)), (e, p, ignored1, ignored2, projection) -> {
			if (ModelPath.root().isDirectDescendant(p)) {
				modeRegistry.register(propertyFactory.create(ModelPropertyIdentifier.of(ModelPropertyIdentifier.of(ProjectIdentifier.of(project),"components"), p.getName()), e));
			}
		}));

		project.getExtensions().add(ComponentVariantsPropertyRegistrationFactory.class, "__nokee_componentVariantsPropertyFactory", new ComponentVariantsPropertyRegistrationFactory(project.getExtensions().getByType(ModelRegistry.class), project.getExtensions().getByType(ModelPropertyRegistrationFactory.class), project.getProviders(), project.getExtensions().getByType(ModelLookup.class)));
		project.getExtensions().add(ComponentSourcesPropertyRegistrationFactory.class, "__nokee_componentSourcesPropertyFactory", new ComponentSourcesPropertyRegistrationFactory(project.getExtensions().getByType(ModelRegistry.class), project.getExtensions().getByType(ModelPropertyRegistrationFactory.class), project.getExtensions().getByType(ModelConfigurer.class)));
		project.getExtensions().add(ComponentDependenciesPropertyRegistrationFactory.class, "__nokee_componentDependenciesPropertyFactory", new ComponentDependenciesPropertyRegistrationFactory(project.getExtensions().getByType(ModelRegistry.class), project.getExtensions().getByType(ModelPropertyRegistrationFactory.class), project.getExtensions().getByType(ModelConfigurer.class), project.getExtensions().getByType(ModelLookup.class)));
		project.getExtensions().add(ComponentBinariesPropertyRegistrationFactory.class, "__nokee_componentBinariesPropertyFactory", new ComponentBinariesPropertyRegistrationFactory(project.getExtensions().getByType(ModelRegistry.class), project.getExtensions().getByType(ModelPropertyRegistrationFactory.class), project.getExtensions().getByType(ModelConfigurer.class), project.getProviders(), project.getExtensions().getByType(ModelLookup.class)));
		project.getExtensions().add(ComponentTasksPropertyRegistrationFactory.class, "__nokee_componentTasksPropertyFactory", new ComponentTasksPropertyRegistrationFactory(project.getExtensions().getByType(ModelRegistry.class), project.getExtensions().getByType(ModelPropertyRegistrationFactory.class), project.getExtensions().getByType(ModelConfigurer.class), project.getProviders(), project.getExtensions().getByType(ModelLookup.class)));

		project.getExtensions().add("__nokee_declarableBucketFactory", new DeclarableDependencyBucketRegistrationFactory(NamedDomainObjectRegistry.of(project.getConfigurations()), new DefaultDependencyBucketFactory(NamedDomainObjectRegistry.of(project.getConfigurations()), DependencyFactory.forProject(project))));
		project.getExtensions().add("__nokee_resolvableBucketFactory", new ResolvableDependencyBucketRegistrationFactory(NamedDomainObjectRegistry.of(project.getConfigurations()), new DefaultDependencyBucketFactory(NamedDomainObjectRegistry.of(project.getConfigurations()), DependencyFactory.forProject(project))));
		project.getExtensions().add("__nokee_consumableBucketFactory", new ConsumableDependencyBucketRegistrationFactory(NamedDomainObjectRegistry.of(project.getConfigurations()), new DefaultDependencyBucketFactory(NamedDomainObjectRegistry.of(project.getConfigurations()), DependencyFactory.forProject(project)), project.getObjects()));

		project.getExtensions().add(DimensionPropertyRegistrationFactory.class, "__nokee_dimensionPropertyFactory", new DimensionPropertyRegistrationFactory(project.getObjects(), project.getExtensions().getByType(ModelLookup.class)));
		project.getExtensions().add(TaskRegistrationFactory.class, "__nokee_taskRegistrationFactory", new TaskRegistrationFactory( PolymorphicDomainObjectRegistry.of(project.getTasks()), identifier -> taskName(identifier)));
	}

	// TODO: Move Namer into its own class. We should also do the same for configuration name
	public static String taskName(TaskIdentifier<?> identifier) {
		return identifier.getName().getVerb() + Streams.stream(identifier)
			.flatMap(it -> {
				if (it instanceof ProjectIdentifier) {
					return Stream.empty();
				} else if (it instanceof ComponentIdentifier) {
					if (((ComponentIdentifier) it).isMainComponent()) {
						return Stream.empty();
					} else {
						return Stream.of(((ComponentIdentifier) it).getName().get());
					}
				} else if (it instanceof ComponentIdentity) {
					if (((ComponentIdentity) it).isMainComponent()) {
						return Stream.empty();
					} else {
						return Stream.of(((ComponentIdentity) it).getName().get());
					}
				} else if (it instanceof VariantIdentifier) {
					return Stream.of(((VariantIdentifier<?>) it).getUnambiguousName());
				} else if (it instanceof HasName) {
					val name = ((HasName) it).getName();
					if (name instanceof TaskName) {
						return Streams.stream(((TaskName) name).getObject());
					} else {
						return Stream.of(name.toString());
					}
				} else {
					throw new UnsupportedOperationException();
				}
			})
			.map(StringUtils::capitalize)
			.collect(Collectors.joining());
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
