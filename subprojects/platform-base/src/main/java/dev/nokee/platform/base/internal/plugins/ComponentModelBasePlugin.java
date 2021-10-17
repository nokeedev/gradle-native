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

import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;
import dev.nokee.internal.Factory;
import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.internal.IsLanguageSourceSet;
import dev.nokee.language.base.internal.plugins.LanguageBasePlugin;
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
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.util.function.Consumer;
import java.util.stream.Collectors;

import static dev.nokee.model.internal.BaseNamedDomainObjectView.namedView;
import static dev.nokee.model.internal.core.ModelActions.executeUsingProjection;
import static dev.nokee.model.internal.core.ModelComponentType.projectionOf;
import static dev.nokee.model.internal.core.ModelNodes.discover;
import static dev.nokee.model.internal.core.ModelNodes.mutate;
import static dev.nokee.model.internal.core.ModelProjections.createdUsing;
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

		val modelRegistry = project.getExtensions().getByType(ModelRegistry.class);
		val components = modelRegistry.register(components(project));
		project.getExtensions().add(ComponentContainer.class, "components", components.as(ComponentContainer.class).get());

		project.getExtensions().add(ComponentVariantsPropertyRegistrationFactory.class, "__nokee_componentVariantsPropertyFactory", new ComponentVariantsPropertyRegistrationFactory(project.getExtensions().getByType(ModelRegistry.class), project.getExtensions().getByType(ModelPropertyRegistrationFactory.class), project.getProviders(), project.getExtensions().getByType(ModelLookup.class)));
		project.getExtensions().add(ComponentSourcesPropertyRegistrationFactory.class, "__nokee_componentSourcesPropertyFactory", new ComponentSourcesPropertyRegistrationFactory(project.getExtensions().getByType(ModelRegistry.class), project.getExtensions().getByType(ModelPropertyRegistrationFactory.class), project.getExtensions().getByType(ModelConfigurer.class)));
	}

	private static ModelRegistration components(Project project) {
		val modelConfigurer = project.getExtensions().getByType(ModelConfigurer.class);
		val path = ModelPath.path("components");
		val ownerPath = ModelPath.root();
		val registry = project.getExtensions().getByType(ModelRegistry.class);
		val propertyFactory = project.getExtensions().getByType(ModelPropertyRegistrationFactory.class);
		val factories = new NodeRegistrationFactories();
		return ModelRegistration.builder()
			.withComponent(path)
			.withComponent(IsModelProperty.class)
			.withComponent(createdUsing(of(ComponentContainer.class), () -> new ComponentContainerAdapter(new ViewAdapter<>(Component.class, new ModelNodeBackedViewStrategy(project.getProviders())), (name, type) -> {
				return registry.register(factories.get(ModelType.of(type)).create(name));
			})))
			.withComponent(factories)
			.action(ModelActionWithInputs.of(ModelComponentReference.of(ModelPath.class), ModelComponentReference.of(ModelState.IsAtLeastRegistered.class), (ee, pp, ignored) -> {
				if (path.equals(pp)) {
					modelConfigurer.configure(ModelActionWithInputs.of(ModelComponentReference.of(ModelPath.class), ModelComponentReference.of(ModelState.IsAtLeastCreated.class), ModelComponentReference.of(IsLanguageSourceSet.class), ModelComponentReference.ofAny(projectionOf(LanguageSourceSet.class)), (e, p, ignored1, ignored2, projection) -> {
						if (ownerPath.isDescendant(p)) {
							val elementName = StringUtils.uncapitalize(Streams.stream(Iterables.skip(p, Iterables.size(ownerPath)))
								.filter(it -> !it.isEmpty())
								.map(StringUtils::capitalize)
								.collect(Collectors.joining()));
							registry.register(propertyFactory.create(path.child(elementName), e));
						}
					}));
				}
			}))
			.build();
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
