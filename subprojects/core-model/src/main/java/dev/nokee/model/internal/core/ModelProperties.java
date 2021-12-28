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

import lombok.val;

import java.util.Optional;
import java.util.stream.Stream;

public final class ModelProperties {
	private ModelProperties() {}

	public static ModelElement getProperty(Object self, String name) {
		return ModelNodes.of(self).getComponent(ModelComponentType.componentOf(DescendantNodes.class)).getDirectDescendants().stream().filter(it -> it.hasComponent(IsModelProperty.class)).filter(it -> it.getComponent(ModelPath.class).getName().equals(name)).findFirst().map(DefaultModelElement::of).orElseThrow(() -> new IllegalArgumentException("No property of name '" + name + "'"));
	}

	public static Optional<ModelElement> findProperty(Object self, String name) {
		return ModelNodes.of(self).getComponent(ModelComponentType.componentOf(DescendantNodes.class)).getDirectDescendants().stream().filter(it -> it.hasComponent(IsModelProperty.class)).filter(it -> it.getComponent(ModelPath.class).getName().equals(name)).findFirst().map(DefaultModelElement::of);
	}

	public static boolean hasProperty(Object self, String name) {
		return findProperty(self, name).isPresent();
	}

	public static Stream<ModelElement> getProperties(Object self) {
		val result = ModelNodes.of(self).getComponent(ModelComponentType.componentOf(DescendantNodes.class)).getDirectDescendants();
		return result.stream().filter(it -> it.hasComponent(ModelComponentType.componentOf(IsModelProperty.class))).map(DefaultModelElement::of);
	}
}
