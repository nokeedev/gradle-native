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
package dev.nokee.model.internal.registry;

import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodeContext;
import dev.nokee.model.internal.core.ModelProjection;
import dev.nokee.model.internal.core.TypeCompatibilityModelProjectionSupport;
import dev.nokee.model.internal.type.ModelType;
import lombok.EqualsAndHashCode;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;

/**
 * A projection that uses a factory to create the view instance.
 *
 * @param <M>  the type of the projection
 */
@EqualsAndHashCode
public final class ProvidedModelProjection<M> implements ModelProjection {
	private final TypeCompatibilityModelProjectionSupport<M> delegate;
	private final ProviderFactory providers;
	private final ModelNode entity;

	public ProvidedModelProjection(TypeCompatibilityModelProjectionSupport<M> delegate, ProviderFactory providers, ModelNode entity) {
		this.delegate = delegate;
		this.providers = providers;
		this.entity = entity;
	}

	@Override
	public ModelType<?> getType() {
		return delegate.getType();
	}

	@Override
	public <T> boolean canBeViewedAs(ModelType<T> type) {
		return delegate.canBeViewedAs(type) || type.getRawType().equals(Provider.class);
	}

	@Override
	public <T> T get(ModelType<T> type) {
		if (type.getRawType().equals(Provider.class)) {
			return type.getConcreteType().cast(providers.provider(() -> ModelNodeContext.of(entity).execute(ignored -> { return delegate.get(ModelType.of(Object.class)); })));
		}
		return type.getConcreteType().cast(delegate.get(type));
	}

	@Override
	public Iterable<String> getTypeDescriptions() {
		return delegate.getTypeDescriptions();
	}
}
