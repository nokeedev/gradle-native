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

import dev.nokee.model.internal.registry.ModelNodeBackedElement;
import lombok.val;

import java.util.Optional;
import java.util.stream.Stream;

public final class ModelProperties {
	private ModelProperties() {}

	public static ModelElement getProperty(Object self, String name) {
		val result = ModelNodes.of(self).getComponent(ModelComponentType.componentOf(DescendantNodes.class)).getDescendant(name);
		if (!result.hasComponent(ModelComponentType.componentOf(IsModelProperty.class))) {
			throw new IllegalArgumentException("Not a property");
		}
		return new ModelNodeBackedElement(result);
	}

	public static Optional<ModelElement> findProperty(Object self, String name) {
		return ModelNodes.of(self).getComponent(ModelComponentType.componentOf(DescendantNodes.class)).findDescendant(name).map(it -> {
			if (!it.hasComponent(ModelComponentType.componentOf(IsModelProperty.class))) {
				throw new IllegalArgumentException("Not a property");
			}
			return new ModelNodeBackedElement(it);
		});
	}

	public static boolean hasProperty(Object self, String name) {
		return ModelNodes.of(self).getComponent(ModelComponentType.componentOf(DescendantNodes.class)).findDescendant(name).filter(it -> it.hasComponent(ModelComponentType.componentOf(IsModelProperty.class))).isPresent();

	}

	public static Stream<ModelElement> getProperties(Object self) {
		val result = ModelNodes.of(self).getComponent(ModelComponentType.componentOf(DescendantNodes.class)).getDirectDescendants();
		return result.stream().filter(it -> it.hasComponent(ModelComponentType.componentOf(IsModelProperty.class))).map(ModelNodeBackedElement::new);
	}
}
