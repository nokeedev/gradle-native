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

import java.util.Objects;

import static dev.nokee.model.internal.type.ModelTypeUtils.toUndecoratedType;

public class ModelComponentType<T> {

	public static <T> ModelComponentType<? super T> ofInstance(T component) {
		Objects.requireNonNull(component);
		if (component instanceof ModelProjection) {
			return (ModelComponentType<? super T>) projectionOf(((ModelProjection) component).getType().getRawType());
		} else {
			return (ModelComponentType<? super T>) componentOf(toUndecoratedType(component.getClass()));
		}
	}

	public static <T> ModelComponentType<T> componentOf(Class<T> type) {
		Objects.requireNonNull(type);
		return new ComponentType<>(type);
	}

	public static <T> ModelComponentType<ModelProjection> projectionOf(Class<T> type) {
		Objects.requireNonNull(type);
		return new ProjectionType<>(type);
	}

	@Value
	@EqualsAndHashCode(callSuper = false)
	private static class ComponentType<T> extends ModelComponentType<T> {
		Class<T> value;
	}

	@Value
	@EqualsAndHashCode(callSuper = false)
	private static class ProjectionType<T> extends ModelComponentType<ModelProjection> {
		Class<T> value;
	}
}
