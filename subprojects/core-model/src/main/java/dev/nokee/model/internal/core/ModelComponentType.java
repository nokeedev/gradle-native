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

import lombok.EqualsAndHashCode;
import lombok.Value;

import static dev.nokee.model.internal.type.ModelTypeUtils.toUndecoratedType;

public class ModelComponentType {
	public static ModelComponentType ofInstance(Object component) {
		if (component instanceof TypeCompatibilityModelProjectionSupport) {
			return projectionOf(((TypeCompatibilityModelProjectionSupport<?>) component).getType().getRawType());
		} else {
			return componentOf(toUndecoratedType(component.getClass()));
		}
	}

	public static ModelComponentType componentOf(Class<?> type) {
		return new ComponentType(type);
	}

	public static ModelComponentType projectionOf(Class<?> type) {
		return new ProjectionType(type);
	}

	@Value
	@EqualsAndHashCode(callSuper = false)
	private static class ComponentType extends ModelComponentType {
		Class<?> value;
	}

	@Value
	@EqualsAndHashCode(callSuper = false)
	private static class ProjectionType extends ModelComponentType {
		Class<?> value;
	}
}
