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

import com.google.common.collect.ImmutableSet;
import dev.nokee.gradle.NamedDomainObjectProviderFactory;
import dev.nokee.gradle.NamedDomainObjectProviderSpec;
import dev.nokee.model.KnownDomainObject;
import dev.nokee.model.internal.actions.ModelAction;
import dev.nokee.model.internal.actions.ModelSpec;
import dev.nokee.model.internal.core.GradlePropertyComponent;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodeContext;
import dev.nokee.model.internal.names.RelativeName;
import dev.nokee.platform.base.internal.elements.ComponentElementTypeComponent;
import dev.nokee.platform.base.internal.elements.ComponentElementsFilterComponent;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;

import java.util.Set;

import static dev.nokee.model.internal.actions.ModelSpec.descendantOf;
import static dev.nokee.model.internal.core.ModelNodeUtils.instantiate;
import static dev.nokee.utils.TransformerUtils.noOpTransformer;

public final class ModelNodeBackedViewStrategy implements ViewAdapter.Strategy {
	private static final Runnable NO_OP_REALIZE = () -> {};
	private final Runnable realize;
	private final ModelNode entity;
	private final ProviderFactory providerFactory;

	public ModelNodeBackedViewStrategy(ProviderFactory providerFactory) {
		this.providerFactory = providerFactory;
		this.entity = ModelNodeContext.getCurrentModelNode();
		this.realize = NO_OP_REALIZE;
	}

	public ModelNodeBackedViewStrategy(ProviderFactory providerFactory, Runnable realize) {
		this(providerFactory, realize, ModelNodeContext.getCurrentModelNode());
	}

	public ModelNodeBackedViewStrategy(ProviderFactory providerFactory, Runnable realize, ModelNode entity) {
		this.providerFactory = providerFactory;
		this.realize = new RunOnceRunnable(realize);
		this.entity = entity;
	}

	@Override
	public <T> void configureEach(Class<T> elementType, Action<? super T> action) {
		val descendantOfSpec = descendantOf(entity.get(ViewConfigurationBaseComponent.class).get().getId());
		if (entity.has(BaseModelSpecComponent.class)) {
			instantiate(entity, ModelAction.configureEach(entity.get(BaseModelSpecComponent.class).get().and(descendantOfSpec), elementType, action));
		} else {
			instantiate(entity, ModelAction.configureEach(descendantOfSpec, elementType, action));
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> Provider<Set<T>> getElements(Class<T> elementType) {
		return providerFactory.provider(() -> {
			realize.run(); // TODO: Should move to some provider source or something

			// FIXME: We should accumulate the elements in another component so can put ad-hoc View element
			if (entity.get(ComponentElementTypeComponent.class).get().getType().equals(elementType)) {
				return ((MapProperty<String, T>) entity.get(GradlePropertyComponent.class).get())
					.map(it -> ImmutableSet.copyOf(it.values()));
			}
			return entity.get(ComponentElementsFilterComponent.class).get(elementType);
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
