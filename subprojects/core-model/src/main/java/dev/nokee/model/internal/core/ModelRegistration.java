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

import com.google.common.collect.ImmutableList;
import dev.nokee.internal.Factory;
import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.internal.type.ModelType;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A model registration request.
 */
@EqualsAndHashCode
public final class ModelRegistration {
	private final List<Object> components;

	private ModelRegistration(List<Object> components) {
		this.components = components;
	}

	public static ModelRegistration of(String path, Class<?> type) {
		return builder()
			.withComponent(new ModelPathComponent(ModelPath.path(path)))
			.withComponent(ModelProjections.managed(ModelType.of(type)))
			.build();
	}

	public static Builder managedBuilder(DomainObjectIdentifier identifier, Class<?> type) {
		Objects.requireNonNull(identifier);
		return builder()
			.withComponent(new IdentifierComponent(identifier))
			.withComponent(ModelProjections.managed(ModelType.of(type)));
	}

	public static <T> ModelRegistration bridgedInstance(ModelIdentifier<T> identifier, T instance) {
		return builder()
			.withComponent(new ModelPathComponent(identifier.getPath()))
			.withComponent(ModelProjections.ofInstance(instance))
			.build();
	}

	public static <T> ModelRegistration unmanagedInstance(ModelIdentifier<T> identifier, Factory<T> factory) {
		return builder()
			.withComponent(new ModelPathComponent(identifier.getPath()))
			.withComponent(ModelProjections.createdUsing(identifier.getType(), factory))
			.build();
	}

	public static <T> Builder unmanagedInstanceBuilder(ModelIdentifier<T> identifier, Factory<T> factory) {
		return builder()
			.withComponent(new ModelPathComponent(identifier.getPath()))
			.withComponent(ModelProjections.createdUsing(identifier.getType(), factory));
	}

	public static Builder builder() {
		return new Builder();
	}

	public List<Object> getComponents() {
		return components;
	}

	public static final class Builder {
		private final List<Object> components = new ArrayList<>();

		Builder withComponent(Object component) {
			if (!components.contains(component)) {
				components.add(Objects.requireNonNull(component));
			}
			return this;
		}

		public Builder withComponent(ModelComponent component) {
			return withComponent((Object) component);
		}

		public Builder withComponent(ModelProjection component) {
			return withComponent((Object) component);
		}

		public Builder mergeFrom(ModelRegistration other) {
			components.addAll(other.getComponents());
			return this;
		}

		public ModelRegistration build() {
			return new ModelRegistration(ImmutableList.copyOf(components));
		}
	}
}
