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
package dev.nokee.model.internal;

import com.google.common.collect.Streams;
import dev.nokee.model.DomainObjectProvider;
import dev.nokee.model.internal.core.DisplayName;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.type.ModelType;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class ModelBackedTypeCastOperatorStrategy implements TypeCastOperatorStrategy {
	private final Supplier<DisplayName> displayNameSupplier;
	private final ModelNode entity;
	private final CastableTypes castableTypes;

	public ModelBackedTypeCastOperatorStrategy(Supplier<DisplayName> displayNameSupplier, ModelNode entity, CastableTypes castableTypes) {
		this.displayNameSupplier = Objects.requireNonNull(displayNameSupplier);
		this.entity = Objects.requireNonNull(entity);
		this.castableTypes = Objects.requireNonNull(castableTypes);
	}

	@Override
	public <S> DomainObjectProvider<S> castTo(ModelType<S> type) {
		assert type != null;
		return DefaultModelObject.of(tryFind(type).orElseThrow(() -> castException(displayNameSupplier.get(), type, castableTypes)), entity);
	}

	@SuppressWarnings("unchecked")
	private <S> Optional<ModelType<S>> tryFind(ModelType<S> type) {
		return Streams.stream(castableTypes).filter(type::isAssignableFrom).map(it -> (ModelType<S>) it).findFirst();
	}

	private static ClassCastException castException(DisplayName displayName, ModelType<?> castType, CastableTypes castableTypes) {
		assert displayName != null;
		return new ClassCastException(String.format("Could not cast %s to %s. Available instances: %s.", displayName, castType.getConcreteType().getSimpleName(), Streams.stream(castableTypes).map(it -> it.getConcreteType().getSimpleName()).collect(Collectors.joining(", "))));
	}
}
