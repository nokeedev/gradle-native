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
import dev.nokee.model.internal.ancestors.AncestorRef;
import dev.nokee.model.internal.ancestors.AncestorsComponent;
import dev.nokee.model.internal.core.GradlePropertyComponent;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelComponentReference;
import dev.nokee.model.internal.core.ModelElementProviderSourceComponent;
import dev.nokee.model.internal.core.ModelNodeUtils;
import dev.nokee.model.internal.core.ParentComponent;
import dev.nokee.model.internal.names.RelativeName;
import dev.nokee.model.internal.names.RelativeNamesComponent;
import dev.nokee.model.internal.registry.ModelConfigurer;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.model.internal.tags.ModelTags;
import dev.nokee.model.internal.type.ModelType;
import dev.nokee.platform.base.internal.ModelNodeBackedViewStrategy;
import dev.nokee.platform.base.internal.ViewAdapter;
import dev.nokee.platform.base.internal.ViewConfigurationBaseComponent;
import dev.nokee.utils.Cast;
import lombok.val;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Plugin;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.PluginAware;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.ProviderFactory;

import javax.inject.Inject;

import static dev.nokee.model.internal.core.ModelProjections.createdUsing;
import static dev.nokee.model.internal.type.ModelType.of;

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

	@Override
	@SuppressWarnings("unchecked")
	public void apply(T target) {
		target.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelTags.referenceOf(ComponentElementsTag.class), ModelComponentReference.of(ComponentElementTypeComponent.class), ModelComponentReference.of(ParentComponent.class), (entity, tag, elementType, parent) -> {
			entity.addComponent(createdUsing(Cast.uncheckedCastBecauseOfTypeErasure(viewFor(elementType.get())), () -> new ViewAdapter<>(elementType.get().getConcreteType(), new ModelNodeBackedViewStrategy(providers, () -> ModelStates.finalize(parent.get())))));
		}));
		target.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelTags.referenceOf(ComponentElementsTag.class), ModelComponentReference.of(ViewConfigurationBaseComponent.class), ModelComponentReference.of(ComponentElementTypeComponent.class), ModelComponentReference.of(GradlePropertyComponent.class), (entity, tag, base, elementType, property) -> {
			((MapProperty<String, Object>) property.get()).set(providers.provider(() -> {
				@SuppressWarnings("unchecked")
				val result = (MapProperty<String, Object>) objects.mapProperty(String.class, elementType.get().getConcreteType());
				target.getExtensions().getByType(ModelLookup.class)
					.query(it -> ModelNodeUtils.canBeViewedAs(it, elementType.get()) && it.find(AncestorsComponent.class).map(t -> t.get().contains(AncestorRef.of(base.get()))).orElse(false)).forEach(it -> {
						val nameOptional = it.find(RelativeNamesComponent.class).map(t -> t.get().get(RelativeName.BaseRef.of(base.get())).toString());

						nameOptional.ifPresent(name -> {
							if (ModelNodeUtils.canBeViewedAs(it, objectProviderOf(elementType.get()))) {
								result.put(name, ModelNodeUtils.get(it, objectProviderOf(elementType.get())).map(t -> {
									ModelStates.realize(it);
									return ModelNodeUtils.get(it, elementType.get());
								}));
							} else if (it.has(ModelElementProviderSourceComponent.class)) {
								result.put(name, it.get(ModelElementProviderSourceComponent.class).get().map(t -> {
									ModelStates.realize(it);
									return ModelNodeUtils.get(it, elementType.get());
								}));
							} else {
								result.put(name, providers.provider(() -> {
									ModelStates.realize(it);
									return ModelNodeUtils.get(it, elementType.get());
								}));
							}
						});
					});
				return result;
			}).flatMap(it -> it));
			entity.addComponent(new ComponentElementsFilterComponent(providers, objects, target.getExtensions().getByType(ModelLookup.class), base.get()));
		}));
	}

	@SuppressWarnings("unchecked")
	private static <T> ModelType<NamedDomainObjectProvider<T>> objectProviderOf(ModelType<T> elementType) {
		return (ModelType<NamedDomainObjectProvider<T>>) ModelType.of(new TypeToken<NamedDomainObjectProvider<T>>() {}.where(new TypeParameter<T>() {}, elementType.getConcreteType()).getType());
	}
}
