/*
 * Copyright 2021 the original author or authors.
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
package dev.nokee.platform.base.internal;

import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.KnownDomainObject;
import dev.nokee.model.internal.KnownModelObjectTypeOf;
import dev.nokee.model.internal.ModelElementSupport;
import dev.nokee.model.internal.ModelMap;
import dev.nokee.model.internal.ModelObjectIdentifier;
import dev.nokee.model.internal.ModelObjectIdentifiers;
import dev.nokee.model.internal.TypeFilteringAction;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Transformer;
import org.gradle.api.provider.Provider;

import java.util.Set;

public final class ModelNodeBackedViewStrategy implements ViewAdapter.Strategy {
	private final ModelMap<?> collection;
	private final ModelObjectIdentifier baseIdentifier;

	public ModelNodeBackedViewStrategy(ModelMap<?> collection, ModelObjectIdentifier baseIdentifier) {
		this.collection = collection;
		this.baseIdentifier = baseIdentifier;
	}

	@Override
	public <T> void configureEach(Class<T> elementType, Action<? super T> action) {
		// FIXME(discovery): Use unpackable action
		collection.configureEach(TypeFilteringAction.ofType(elementType, object -> {
			ModelElementSupport.safeAsModelElement(object).ifPresent(element -> {
				if (ModelObjectIdentifiers.descendantOf(element.getIdentifier(), baseIdentifier)) {
					action.execute(elementType.cast(object));
				}
			});
		}));
	}

	@Override
	public <T> Provider<Set<T>> getElements(Class<T> elementType) {
		return collection.getElements(elementType, knownElement -> {
			if (knownElement.getType().isSubtypeOf(elementType)) {
				return ModelObjectIdentifiers.descendantOf(knownElement.getIdentifier(), baseIdentifier);
			}
			return false;
		});
	}

	@Override
	public <T> void whenElementKnown(Class<T> elementType, Action<? super KnownDomainObject<T>> action) {
		throw new UnsupportedOperationException("not supported for now");
	}

	@Override
	public <T> NamedDomainObjectProvider<T> named(String name, Class<T> elementType) {
		throw new UnsupportedOperationException("not supported for now");
//		if (!((MapProperty<String, Object>) entity.get(GradlePropertyComponent.class).get()).get().containsKey(name)) {
//			throw new RuntimeException();
//		}
//		return (NamedDomainObjectProvider<T>) new NamedDomainObjectProviderFactory().create(NamedDomainObjectProviderSpec.builder().named(name)
//			.typedAs(elementType)
//			.configureUsing(action -> {
//				val relativeNameSpec = ModelSpec.has(RelativeName.of(entity.get(ViewConfigurationBaseComponent.class).get().getId(), name));
//				if (entity.has(BaseModelSpecComponent.class)) {
//					instantiate(entity, ModelAction.configureEach(entity.get(BaseModelSpecComponent.class).get().and(relativeNameSpec), elementType, action));
//				} else {
//					instantiate(entity, ModelAction.configureEach(relativeNameSpec, elementType, action));
//				}
//			})
//			.delegateTo(providerFactory.provider(() -> {
//					realize.run();
//					return new Object();
//				})
//				.flatMap(it -> {
//					return ((MapProperty<String, Object>) entity.get(GradlePropertyComponent.class).get()).getting(name);
//				}))
//			.build());
	}
}
