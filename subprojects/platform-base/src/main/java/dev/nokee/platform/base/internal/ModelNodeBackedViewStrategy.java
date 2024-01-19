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

import dev.nokee.model.KnownDomainObject;
import dev.nokee.model.internal.ModelElementSupport;
import dev.nokee.model.internal.ModelObjectIdentifier;
import dev.nokee.model.internal.ModelObjectIdentifiers;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectCollection;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Namer;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.provider.SetProperty;

import java.util.Set;
import java.util.function.Supplier;

import static dev.nokee.utils.TransformerUtils.noOpTransformer;

public final class ModelNodeBackedViewStrategy implements ViewAdapter.Strategy {
	private final NamedDomainObjectCollection<?> collection; // FIXME(discovery): Use ModelMap instead
	private final Runnable realize;
	private final ProviderFactory providerFactory;
	private final ObjectFactory objects;
	private final Namer<? super Object> namer;
	private final Supplier<ModelObjectIdentifier> baseIdentifierSupplier;

	public ModelNodeBackedViewStrategy(Namer<? super Object> namer, NamedDomainObjectCollection<?> collection, ProviderFactory providerFactory, ObjectFactory objects, Runnable realize, ModelObjectIdentifier baseIdentifier) {
		this.namer = namer;
		this.collection = collection;
		this.providerFactory = providerFactory;
		this.objects = objects;
		this.realize = new RunOnceRunnable(realize);
		this.baseIdentifierSupplier = () -> baseIdentifier;
	}

	@Override
	public <T> void configureEach(Class<T> elementType, Action<? super T> action) {
		// FIXME(discovery): Use unpackable action
		collection.configureEach(object -> {
			if (elementType.isInstance(object)) {
				ModelElementSupport.safeAsModelElement(object).ifPresent(element -> {
					final ModelObjectIdentifier baseIdentifier = baseIdentifierSupplier.get();
					if (ModelObjectIdentifiers.descendantOf(element.getIdentifier(), baseIdentifier)) {
						action.execute(elementType.cast(object));
					}
				});
			}
		});
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> Provider<Set<T>> getElements(Class<T> elementType) {
		return providerFactory.provider(() -> {
			realize.run(); // TODO: Should move to some provider source or something

			final SetProperty<T> result = objects.setProperty(elementType);
			collection.matching(object -> {
				if (elementType.isInstance(object)) {
					return ModelElementSupport.safeAsModelElement(object).map(element -> {
						final ModelObjectIdentifier baseIdentifier = baseIdentifierSupplier.get();
						if (ModelObjectIdentifiers.descendantOf(element.getIdentifier(), baseIdentifier)) {
							return true;
						}
						return false;
					}).orElse(false);
				}
				return false;
			}).forEach(it -> {
				result.add(((NamedDomainObjectCollection<? super T>) collection).named(namer.determineName(it), elementType));
			});

			return result;
		}).flatMap(noOpTransformer());
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

	private static final class RunOnceRunnable implements Runnable {
		private Runnable delegate;

		public RunOnceRunnable(Runnable delegate) {
			this.delegate = delegate;
		}

		@Override
		public void run() {
			if (delegate != null) {
				val runnable = delegate;
				delegate = null;
				runnable.run();
			}
		}
	}
}
