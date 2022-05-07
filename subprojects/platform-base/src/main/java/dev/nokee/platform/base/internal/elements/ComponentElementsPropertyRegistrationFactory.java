/*
 * Copyright 2022 the original author or authors.
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
package dev.nokee.platform.base.internal.elements;

import dev.nokee.model.internal.actions.ConfigurableTag;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelPropertyTag;
import dev.nokee.model.internal.core.ModelPropertyTypeComponent;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.type.ModelType;
import dev.nokee.platform.base.internal.ViewConfigurationBaseComponent;

import static dev.nokee.model.internal.tags.ModelTags.tag;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.model.internal.type.ModelTypes.map;

public final class ComponentElementsPropertyRegistrationFactory {
	public Builder newProperty() {
		return new Builder();
	}

	public static final class Builder {
		private ModelNode baseEntity;
		private ModelType<?> elementType;

		public Builder baseRef(ModelNode baseEntity) {
			this.baseEntity = baseEntity;
			return this;
		}

		public Builder elementType(ModelType<?> elementType) {
			this.elementType = elementType;
			return this;
		}

		public ModelRegistration build() {
			return ModelRegistration.builder()
				.withComponent(tag(ModelPropertyTag.class))
				.withComponent(tag(ConfigurableTag.class))
				.withComponent(tag(ComponentElementsTag.class))
				.withComponent(new ViewConfigurationBaseComponent(baseEntity))
				.withComponent(new ComponentElementTypeComponent(elementType))
				.withComponent(new ModelPropertyTypeComponent(map(of(String.class), elementType)))
				.build();
		}
	}
}
