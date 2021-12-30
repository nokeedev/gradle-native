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
package dev.nokee.model.internal.core;

import com.google.common.collect.Streams;
import dev.nokee.model.DomainObjectProvider;
import dev.nokee.model.internal.DefaultModelObject;
import dev.nokee.model.internal.type.ModelType;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public final class ModelBackedModelCastableStrategy implements ModelCastableStrategy {
	private final ModelNode entity;

	public ModelBackedModelCastableStrategy(ModelNode entity) {
		this.entity = entity;
	}

	@Override
	public <S> DomainObjectProvider<S> castTo(ModelType<S> type) {
		assert type != null;
		return DefaultModelObject.of(tryFind(type).orElseThrow(() -> castException(displayName(entity), type, castableTypes(entity).collect(toList()))), entity);
	}

	@SuppressWarnings("unchecked")
	private <S> Optional<ModelType<S>> tryFind(ModelType<S> type) {
		return castableTypes(entity).filter(type::isAssignableFrom).map(it -> (ModelType<S>) it).findFirst();
	}

	private static DisplayName displayName(ModelNode entity) {
		return new DisplayName(entity.getComponent(DisplayNameComponent.class).get());
	}

	private static ClassCastException castException(DisplayName displayName, ModelType<?> castType, Iterable<ModelType<?>> castableTypes) {
		assert displayName != null;
		return new ClassCastException(String.format("Could not cast %s to %s. Available instances: %s.", displayName, castType.getConcreteType().getSimpleName(), Streams.stream(castableTypes).map(it -> it.getConcreteType().getSimpleName()).collect(Collectors.joining(", "))));
	}

	@Override
	public boolean instanceOf(ModelType<?> type) {
		assert type != null;
		return castableTypes(entity).anyMatch(type::isAssignableFrom);
	}

	private static Stream<ModelType<?>> castableTypes(ModelNode entity) {
		return ModelNodeUtils.getProjections(entity).map(ModelProjection::getType);
	}
}
