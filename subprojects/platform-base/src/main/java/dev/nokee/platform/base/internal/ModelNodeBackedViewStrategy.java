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

import dev.nokee.gradle.NamedDomainObjectProviderFactory;
import dev.nokee.gradle.NamedDomainObjectProviderSpec;
import dev.nokee.model.KnownDomainObject;
import dev.nokee.model.internal.ModelElementSupport;
import dev.nokee.model.internal.ModelObjectIdentifier;
import dev.nokee.model.internal.ModelObjectIdentifiers;
import dev.nokee.model.internal.actions.ModelAction;
import dev.nokee.model.internal.actions.ModelSpec;
import dev.nokee.model.internal.core.GradlePropertyComponent;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodeContext;
import dev.nokee.model.internal.names.RelativeName;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectCollection;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Namer;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.provider.SetProperty;

import java.util.Set;

import static dev.nokee.model.internal.actions.ModelSpec.descendantOf;
import static dev.nokee.model.internal.core.ModelNodeUtils.instantiate;
import static dev.nokee.utils.TransformerUtils.noOpTransformer;

public final class ModelNodeBackedViewStrategy implements ViewAdapter.Strategy {
	private static final Runnable NO_OP_REALIZE = () -> {};
	private final NamedDomainObjectCollection<?> collection;
	private final Runnable realize;
	private final ModelNode entity;
	private final ProviderFactory providerFactory;
	private final ObjectFactory objects;
	private final Namer<? super Object> namer;

	public ModelNodeBackedViewStrategy(Namer<? super Object> namer, NamedDomainObjectCollection<?> collection, ProviderFactory providerFactory, ObjectFactory objects) {
		this.namer = namer;
		this.collection = collection;
		this.providerFactory = providerFactory;
		this.objects = objects;
		this.entity = ModelNodeContext.getCurrentModelNode();
		this.realize = NO_OP_REALIZE;
	}

	public ModelNodeBackedViewStrategy(Namer<? super Object> namer, NamedDomainObjectCollection<?> collection, ProviderFactory providerFactory, ObjectFactory objects, Runnable realize) {
		this(namer, collection, providerFactory, objects, realize, ModelNodeContext.getCurrentModelNode());
	}

	public ModelNodeBackedViewStrategy(Namer<? super Object> namer, NamedDomainObjectCollection<?> collection, ProviderFactory providerFactory, ObjectFactory objects, Runnable realize, ModelNode entity) {
		this.namer = namer;
		this.collection = collection;
		this.providerFactory = providerFactory;
		this.objects = objects;
		this.realize = new RunOnceRunnable(realize);
		this.entity = entity;
	}

	@Override
	public <T> void configureEach(Class<T> elementType, Action<? super T> action) {
		final ModelNode baseRef = entity.get(ViewConfigurationBaseComponent.class).get();
		collection.configureEach(object -> {
			if (elementType.isInstance(object)) {
				ModelElementSupport.safeAsModelElement(object).ifPresent(element -> {
					final ModelObjectIdentifier baseIdentifier = baseRef.get(IdentifierComponent.class).get();
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
			final ModelNode baseRef = entity.get(ViewConfigurationBaseComponent.class).get();
			collection.matching(object -> {
				if (elementType.isInstance(object)) {
					return ModelElementSupport.safeAsModelElement(object).map(element -> {
						final ModelObjectIdentifier baseIdentifier = baseRef.get(IdentifierComponent.class).get();
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
		val descendantOfSpec = descendantOf(entity.get(ViewConfigurationBaseComponent.class).get().getId());
		if (entity.has(BaseModelSpecComponent.class)) {
			instantiate(entity, ModelAction.whenElementKnown(entity.get(BaseModelSpecComponent.class).get().and(descendantOfSpec), elementType, action));
		} else {
			instantiate(entity, ModelAction.whenElementKnown(descendantOfSpec, elementType, action));
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> NamedDomainObjectProvider<T> named(String name, Class<T> elementType) {
		if (!((MapProperty<String, Object>) entity.get(GradlePropertyComponent.class).get()).get().containsKey(name)) {
			throw new RuntimeException();
		}
		return (NamedDomainObjectProvider<T>) new NamedDomainObjectProviderFactory().create(NamedDomainObjectProviderSpec.builder().named(name)
			.typedAs(elementType)
			.configureUsing(action -> {
				val relativeNameSpec = ModelSpec.has(RelativeName.of(entity.get(ViewConfigurationBaseComponent.class).get().getId(), name));
				if (entity.has(BaseModelSpecComponent.class)) {
					instantiate(entity, ModelAction.configureEach(entity.get(BaseModelSpecComponent.class).get().and(relativeNameSpec), elementType, action));
				} else {
					instantiate(entity, ModelAction.configureEach(relativeNameSpec, elementType, action));
				}
			})
			.delegateTo(providerFactory.provider(() -> {
					realize.run();
					return new Object();
				})
				.flatMap(it -> {
					return ((MapProperty<String, Object>) entity.get(GradlePropertyComponent.class).get()).getting(name);
				}))
			.build());
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
