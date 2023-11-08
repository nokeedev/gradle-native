/*
 * Copyright 2022 the original author or authors.
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
package dev.nokee.platform.base.internal.elements;

import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;
import dev.nokee.model.internal.ModelObjectIdentifiers;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelComponentReference;
import dev.nokee.model.internal.core.ParentComponent;
import dev.nokee.model.internal.registry.ModelConfigurer;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.model.internal.tags.ModelTags;
import dev.nokee.model.internal.type.ModelType;
import dev.nokee.platform.base.Artifact;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.Variant;
import dev.nokee.platform.base.internal.MainProjectionComponent;
import dev.nokee.platform.base.internal.ModelNodeBackedViewStrategy;
import dev.nokee.platform.base.internal.ViewAdapter;
import dev.nokee.utils.Cast;
import org.gradle.api.Named;
import org.gradle.api.NamedDomainObjectCollection;
import org.gradle.api.Namer;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.PluginAware;
import org.gradle.api.provider.ProviderFactory;

import javax.inject.Inject;

import static dev.nokee.model.internal.core.ModelProjections.createdUsing;

public abstract class ComponentElementsCapabilityPlugin<T extends ExtensionAware & PluginAware> implements Plugin<T> {
	private final ProviderFactory providers;
	private final ObjectFactory objects;

	@Inject
	public ComponentElementsCapabilityPlugin(ProviderFactory providers, ObjectFactory objects) {
		this.providers = providers;
		this.objects = objects;
	}

	@SuppressWarnings("unchecked")
	private static <S> ModelType<ViewAdapter<S>> viewFor(ModelType<S> elementType) {
		return (ModelType<ViewAdapter<S>>) ModelType.of(new TypeToken<ViewAdapter<S>>() {}.where(new TypeParameter<S>() {}, elementType.getConcreteType()).getType());
	}

	private static Namer<? extends Object> namerOf(Class<?> elementType) {
		if (Variant.class.isAssignableFrom(elementType)) {
			return new Named.Namer();
		} else if (Artifact.class.isAssignableFrom(elementType)) {
			return new Named.Namer();
		} else if (Component.class.isAssignableFrom(elementType)) {
			return new Named.Namer();
		} else if (Task.class.isAssignableFrom(elementType)) {
			return new Task.Namer();
		} else if (Configuration.class.isAssignableFrom(elementType)) {
			return new Configuration.Namer();
		} else {
			try {
				Class<?> LanguageSourceSet = Class.forName("dev.nokee.language.base.LanguageSourceSet");
				if (LanguageSourceSet.isAssignableFrom(elementType)) {
					return new Named.Namer();
				}
			} catch (ClassNotFoundException e) {
				// ignores
			}
			throw new UnsupportedOperationException("unknown element type for view -- " + elementType.getSimpleName());
		}
	}

	private static NamedDomainObjectCollection<?> collectionOf(ExtensionAware target, Class<?> elementType) {
		if (Variant.class.isAssignableFrom(elementType)) {
			return (NamedDomainObjectCollection<?>) target.getExtensions().getByName("$variants");
		} else if (Artifact.class.isAssignableFrom(elementType)) {
			return (NamedDomainObjectCollection<?>) target.getExtensions().getByName("$artifacts");
		} else if (Component.class.isAssignableFrom(elementType)) {
			return (NamedDomainObjectCollection<?>) target.getExtensions().getByName("$components");
		} else if (Task.class.isAssignableFrom(elementType)) {
			return ((Project) target).getTasks();
		} else if (Configuration.class.isAssignableFrom(elementType)) {
			return ((Project) target).getConfigurations();
		} else {
			try {
				Class<?> LanguageSourceSet = Class.forName("dev.nokee.language.base.LanguageSourceSet");
				if (LanguageSourceSet.isAssignableFrom(elementType)) {
					return (NamedDomainObjectCollection<?>) target.getExtensions().getByName("$sources");
				}
			} catch (ClassNotFoundException e) {
				// ignores
			}
			throw new UnsupportedOperationException("unknown element type for view -- " + elementType.getSimpleName());
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public void apply(T target) {
		target.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelTags.referenceOf(ComponentElementsTag.class), ModelComponentReference.of(ComponentElementTypeComponent.class), ModelComponentReference.of(ParentComponent.class), (entity, tag, elementType, parent) -> {
			entity.addComponent(createdUsing(Cast.uncheckedCastBecauseOfTypeErasure(viewFor(elementType.get())), () -> new ViewAdapter<>(elementType.get().getConcreteType(), new ModelNodeBackedViewStrategy((Namer<? super Object>) namerOf(elementType.get().getConcreteType()), collectionOf(target, elementType.get().getConcreteType()), providers, objects, () -> {
				ModelStates.finalize(parent.get());
				target.getExtensions().getByType(ModelLookup.class).query(it -> it.find(IdentifierComponent.class).map(id -> ModelObjectIdentifiers.descendantOf(id.get(), parent.get().get(IdentifierComponent.class).get())).orElse(false)).forEach(it -> {
					it.find(MainProjectionComponent.class).ifPresent(component -> {
						try {
							Class<?> LanguageSourceSet = Class.forName("dev.nokee.language.base.LanguageSourceSet");
							if (LanguageSourceSet.isAssignableFrom(component.getProjectionType())) {
								ModelStates.finalize(it);
							}
						} catch (ClassNotFoundException e) {
							// ignores
						}
					});
				});
			}))));
		}));
	}
}
