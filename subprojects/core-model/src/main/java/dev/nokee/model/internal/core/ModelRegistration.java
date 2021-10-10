/*
 * Copyright 2020-2021 the original author or authors.
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

import dev.nokee.internal.Factory;
import dev.nokee.model.internal.type.ModelType;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static dev.nokee.model.internal.core.NodePredicate.self;

/**
 * A model registration request.
 *
 * @param <T>  the default projection type, provided for type-safety.
 */
@EqualsAndHashCode
public final class ModelRegistration<T> {
	private final List<ModelAction> actions;
	private final List<Object> components;
	@EqualsAndHashCode.Exclude private final ModelType<T> defaultProjectionType;

	private ModelRegistration(ModelType<T> defaultProjectionType, List<ModelAction> actions, List<Object> components) {
		this.defaultProjectionType = defaultProjectionType;
		this.actions = actions;
		this.components = components;
	}

	public ModelType<T> getDefaultProjectionType() {
		return defaultProjectionType;
	}

	public static <T> ModelRegistration<T> of(String path, Class<T> type) {
		return builder()
			.withComponent(ModelPath.path(path))
			.withDefaultProjectionType(ModelType.of(type))
			.withComponent(ModelProjections.managed(ModelType.of(type)))
			.build();
	}

	public static <T> ModelRegistration<T> bridgedInstance(ModelIdentifier<T> identifier, T instance) {
		return builder()
			.withComponent(identifier.getPath())
			.withDefaultProjectionType(identifier.getType())
			.withComponent(ModelProjections.ofInstance(instance))
			.build();
	}

	public static <T> ModelRegistration<T> unmanagedInstance(ModelIdentifier<T> identifier, Factory<T> factory) {
		return builder()
			.withComponent(identifier.getPath())
			.withDefaultProjectionType(identifier.getType())
			.withComponent(ModelProjections.createdUsing(identifier.getType(), factory))
			.build();
	}

	public static <T> Builder<T> unmanagedInstanceBuilder(ModelIdentifier<T> identifier, Factory<T> factory) {
		return builder()
			.withComponent(identifier.getPath())
			.withDefaultProjectionType(identifier.getType())
			.withComponent(ModelProjections.createdUsing(identifier.getType(), factory));
	}

	public static <T> Builder<T> builder() {
		return new Builder<>();
	}

	public List<Object> getComponents() {
		return Collections.unmodifiableList(components);
	}

	public List<ModelAction> getActions() {
		return Collections.unmodifiableList(actions);
	}

	public static final class Builder<T> {
		private ModelType<? super T> defaultProjectionType = ModelType.untyped();
		private final List<Object> components = new ArrayList<>();
		private final List<ModelAction> actions = new ArrayList<>();

		@SuppressWarnings("unchecked") // take the specified information for granted
		public <S extends T> Builder<S> withDefaultProjectionType(ModelType<S> type) {
			((Builder<S>) this).defaultProjectionType = type;
			return (Builder<S>) this;
		}

		public Builder<T> withComponent(Object component) {
			components.add(Objects.requireNonNull(component));
			return this;
		}

		public Builder<T> action(ModelAction action) {
			actions.add(action);
			return this;
		}

		// take for granted that whatever projection type is, it will project to <T>
		@SuppressWarnings("unchecked")
		public ModelRegistration<T> build() {
			return new ModelRegistration<>((ModelType<T>)defaultProjectionType, actions, components);
		}
	}
}
