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
package dev.nokee.language.base.internal.plugins;

import com.google.common.collect.MoreCollectors;
import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.internal.HasConfigurableSourceMixInRule;
import dev.nokee.language.base.internal.IsLanguageSourceSet;
import dev.nokee.language.base.internal.SourceSetFactory;
import dev.nokee.model.internal.DefaultDomainObjectIdentifier;
import dev.nokee.model.internal.core.DisplayNameComponent;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelComponentReference;
import dev.nokee.model.internal.core.ModelNodeContext;
import dev.nokee.model.internal.core.ModelNodeUtils;
import dev.nokee.model.internal.core.ModelPathComponent;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.core.ParentComponent;
import dev.nokee.model.internal.names.ElementNameComponent;
import dev.nokee.model.internal.plugins.ModelBasePlugin;
import dev.nokee.model.internal.registry.ModelConfigurer;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.model.internal.tags.ModelTags;
import dev.nokee.model.internal.type.ModelType;
import dev.nokee.model.internal.type.TypeOf;
import dev.nokee.platform.base.ComponentSources;
import dev.nokee.platform.base.SourceAwareComponent;
import dev.nokee.platform.base.View;
import dev.nokee.platform.base.internal.ModelBackedSourceAwareComponentMixIn;
import dev.nokee.platform.base.internal.ViewAdapter;
import dev.nokee.platform.base.internal.elements.ComponentElementsPropertyRegistrationFactory;
import dev.nokee.platform.base.internal.plugins.OnDiscover;
import dev.nokee.scripts.DefaultImporter;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;

import static dev.nokee.model.internal.core.ModelProjections.createdUsing;
import static dev.nokee.model.internal.type.ModelType.of;

public class LanguageBasePlugin implements Plugin<Project> {
	@Override
	@SuppressWarnings("unchecked")
	public void apply(Project project) {
		project.getPluginManager().apply(ModelBasePlugin.class);

		DefaultImporter.forProject(project).defaultImport(LanguageSourceSet.class);

		project.getExtensions().add("__nokee_sourceSetFactory", new SourceSetFactory(project.getObjects()));
		project.getExtensions().getByType(ModelConfigurer.class).configure(new OnDiscover(new HasConfigurableSourceMixInRule(project.getExtensions().getByType(SourceSetFactory.class)::sourceSet, project.getExtensions().getByType(ModelRegistry.class), project.getObjects())));

		val elementsPropertyFactory = new ComponentElementsPropertyRegistrationFactory();

		// ComponentFromEntity<DisplayNameComponent> read-only self
		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelTags.referenceOf(IsLanguageSourceSet.class), ModelComponentReference.of(ModelState.IsAtLeastCreated.class), (entity, ignored1, ignored2) -> {
			if (!entity.has(DisplayNameComponent.class)) {
				entity.addComponent(new DisplayNameComponent("sources"));
			}
		}));

		// ComponentFromEntity<ParentComponent> read-only self
		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelTags.referenceOf(IsLanguageSourceSet.class), ModelComponentReference.of(ModelPathComponent.class), ModelComponentReference.of(DisplayNameComponent.class), ModelComponentReference.of(ElementNameComponent.class), ModelComponentReference.of(ModelState.IsAtLeastCreated.class), (entity, ignored1, path, displayName, elementName, ignored2) -> {
			val parentIdentifier = entity.find(ParentComponent.class).map(parent -> parent.get().get(IdentifierComponent.class).get()).orElse(null);
			entity.addComponent(new IdentifierComponent(new DefaultDomainObjectIdentifier(elementName.get(), parentIdentifier, displayName.get(), path.get())));
		}));
		project.getExtensions().getByType(ModelConfigurer.class).configure(new OnDiscover(ModelActionWithInputs.of(ModelComponentReference.ofProjection(ModelType.of(new TypeOf<ModelBackedSourceAwareComponentMixIn<? extends ComponentSources, ? extends ComponentSources>>() {})), ModelComponentReference.of(IdentifierComponent.class), (entity, projection, identifier) -> {
			val registry = project.getExtensions().getByType(ModelRegistry.class);
			Class<ComponentSources> type = (Class<ComponentSources>) sourcesType((ModelType<SourceAwareComponent<? extends ComponentSources>>)projection.getType());
			registry.register(ModelRegistration.builder()
				.withComponent(new ElementNameComponent("sources"))
				.withComponent(new ParentComponent(entity))
				.mergeFrom(elementsPropertyFactory.newProperty().baseRef(entity).elementType(of(LanguageSourceSet.class)).build())
				.withComponent(createdUsing(of(type), () -> {
					try {
						for (Constructor<?> constructor : type.getConstructors()) {
							if (constructor.getParameterTypes().length == 1) {
								if (constructor.getParameterTypes()[0].equals(View.class)) {
									return ((Constructor<ComponentSources>) constructor).newInstance(ModelNodeUtils.get(ModelNodeContext.getCurrentModelNode(), of(new TypeOf<ViewAdapter<? extends LanguageSourceSet>>() {})));
								} else if (constructor.getParameterTypes()[0].equals(ViewAdapter.class)) {
									return ((Constructor<ComponentSources>) constructor).newInstance(ModelNodeUtils.get(ModelNodeContext.getCurrentModelNode(), of(new TypeOf<ViewAdapter<? extends LanguageSourceSet>>() {})));
								}
							}
						}
						throw new UnsupportedOperationException();
					} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
						throw new RuntimeException(e);
					}
				}))
				.build());
		})));
	}

	@SuppressWarnings("unchecked")
	private static Class<? extends ComponentSources> sourcesType(ModelType<? extends SourceAwareComponent<? extends ComponentSources>> type) {
		val t = type.getInterfaces().stream().filter(it -> it.getRawType().equals(ModelBackedSourceAwareComponentMixIn.class)).map(it -> (ModelType<ComponentSources>) it).collect(MoreCollectors.onlyElement());
		val tt = ((ParameterizedType) t.getType()).getActualTypeArguments()[1];
		if (tt instanceof ParameterizedType) {
			return (Class<? extends ComponentSources>) ((ParameterizedType) tt).getRawType();
		} else {
			return (Class<? extends ComponentSources>) tt;
		}
	}

}
