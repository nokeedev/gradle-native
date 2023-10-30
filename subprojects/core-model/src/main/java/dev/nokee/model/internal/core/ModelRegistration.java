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
import dev.nokee.model.internal.DefaultModelObjectIdentifier;
import dev.nokee.model.internal.names.ElementName;
import dev.nokee.model.internal.tags.ModelTag;
import dev.nokee.model.internal.tags.ModelTags;
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
	private final List<ModelComponent> components;

	private ModelRegistration(List<ModelComponent> components) {
		this.components = components;
	}

	public static ModelRegistration of(String path, Class<?> type) {
		return builder()
			.withComponent(new ModelPathComponent(ModelPath.path(path)))
			.withComponent(new IdentifierComponent(new DefaultModelObjectIdentifier(ElementName.of(path))))
			.withComponent(ModelProjections.managed(ModelType.of(type)))
			.build();
	}

	public static Builder builder() {
		return new Builder();
	}

	public List<ModelComponent> getComponents() {
		return components;
	}

	public static final class Builder {
		private final List<ModelComponent> components = new ArrayList<>();

		Builder withComponent(Object component) {
			if (!components.contains(component)) {
				components.add(Objects.requireNonNull((ModelComponent) component));
			}
			return this;
		}

		public Builder withComponent(ModelComponent component) {
			return withComponent((Object) component);
		}

		public Builder withComponent(ModelProjection component) {
			return withComponent((Object) component);
		}

		public <T extends ModelTag> Builder withComponentTag(Class<T> type) {
			return withComponent((Object) ModelTags.tag(type));
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
