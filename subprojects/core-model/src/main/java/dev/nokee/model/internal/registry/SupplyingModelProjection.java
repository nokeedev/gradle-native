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

import dev.nokee.model.internal.core.TypeCompatibilityModelProjectionSupport;
import dev.nokee.model.internal.type.ModelType;
import lombok.EqualsAndHashCode;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * A projection that uses a factory to create the view instance.
 *
 * @param <M>  the type of the projection
 */
@EqualsAndHashCode(callSuper = true)
public final class SupplyingModelProjection<M> extends TypeCompatibilityModelProjectionSupport<M> {
	private final Supplier<M> supplier;

	public SupplyingModelProjection(ModelType<M> type, Supplier<M> supplier) {
		super(type);
		this.supplier = Objects.requireNonNull(supplier);
	}

	@Override
	public <T> T get(ModelType<T> type) {
		return type.getConcreteType().cast(supplier.get());
	}

	@Override
	public String toString() {
		return "SupplyingModelProjection.of(" + getType() + ", " + supplier + ")";
	}
}
