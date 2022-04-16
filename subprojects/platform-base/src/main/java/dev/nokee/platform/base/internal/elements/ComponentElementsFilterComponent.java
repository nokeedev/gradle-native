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

import dev.nokee.model.internal.ancestors.AncestorRef;
import dev.nokee.model.internal.ancestors.AncestorsComponent;
import dev.nokee.model.internal.core.ModelComponent;
import dev.nokee.model.internal.core.ModelElementProviderSourceComponent;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodeUtils;
import dev.nokee.model.internal.names.RelativeName;
import dev.nokee.model.internal.names.RelativeNamesComponent;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.model.internal.state.ModelStates;
import lombok.val;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.provider.SetProperty;

import java.util.Set;

import static dev.nokee.model.internal.type.ModelType.of;

public final class ComponentElementsFilterComponent implements ModelComponent {
	private final ProviderFactory providers;
	private final ObjectFactory objects;
	private final ModelLookup lookup;
	private final ModelNode baseRef;

	public ComponentElementsFilterComponent(ProviderFactory providers, ObjectFactory objects, ModelLookup lookup, ModelNode baseRef) {
		this.providers = providers;
		this.objects = objects;
		this.lookup = lookup;
		this.baseRef = baseRef;
	}

	@SuppressWarnings("unchecked")
	public <T> Provider<Set<T>> get(Class<T> elementType) {
		return providers.provider(() -> {
			final SetProperty<T> result = objects.setProperty(elementType);
			lookup.query(it -> ModelNodeUtils.canBeViewedAs(it, of(elementType)) && it.find(AncestorsComponent.class).map(t -> t.get().contains(AncestorRef.of(baseRef))).orElse(false)).forEach(it -> {
				val nameOptional = it.find(RelativeNamesComponent.class).map(t -> t.get().get(RelativeName.BaseRef.of(baseRef)).toString());

				nameOptional.ifPresent(name -> {
					if (ModelNodeUtils.canBeViewedAs(it, of(NamedDomainObjectProvider.class))) {
						result.add(ModelNodeUtils.get(it, NamedDomainObjectProvider.class).map(t -> {
							ModelStates.realize(it);
							return ModelNodeUtils.get(it, elementType);
						}));
					} else if (it.has(ModelElementProviderSourceComponent.class)) {
						result.add(it.get(ModelElementProviderSourceComponent.class).get().map(t -> {
							ModelStates.realize(it);
							return ModelNodeUtils.get(it, elementType);
						}));
					} else {
						result.add(providers.provider(() -> {
							ModelStates.realize(it);
							return ModelNodeUtils.get(it, elementType);
						}));
					}
				});
			});
			return result;
		}).flatMap(it -> it);
	}
}
