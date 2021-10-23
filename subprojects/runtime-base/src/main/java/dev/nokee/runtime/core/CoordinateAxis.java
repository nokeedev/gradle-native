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
package dev.nokee.runtime.core;

/**
 * An axis in a coordinate space.
 * Each axis specify the base type of the coordinate's value and a name.
 * An axis is equal when both the type and name are the same.
 * This allows users to create multiple axis of the same type.
 *
 * @param <T> the type of the coordinate's values
 */
public interface CoordinateAxis<T> {
	Class<T> getType();

	default String getName() {
		return Coordinates.inferCoordinateAxisNameFromType(getType());
	}

	default String getDisplayName() {
		return Coordinates.inferCoordinateAxisDisplayNameFromType(getType());
	}

	/**
	 * Creates a coordinate for the specified value on this axis.
	 *
	 * @param value  the coordinate value, must not be null
	 * @return the coordinate, never null
	 */
	default Coordinate<T> create(T value) {
		if (value instanceof Coordinate && ((Coordinate<?>) value).getAxis().equals(this)) {
			return (Coordinate<T>) value;
		}
		return new DefaultCoordinate<>(this, value);
	}

	/**
	 * Creates an axis of the specified type.
	 * The axis' name will be inferred from the type's name which is split by words to lower kebab case.
	 * For example, a class name {@literal OperatingSystemFamily} would produce the name {@literal operating-system-family}.
	 *
	 * @param type  the type of the axis, must not be null
	 * @param <T>  the type of the coordinate's values
	 * @return an axis, never null
	 * @see #of(Class, String) to create an axis with a custom name
	 */
	static <T> CoordinateAxis<T> of(Class<T> type) {
		return new DefaultCoordinateAxis<>(type);
	}

	/**
	 * Creates a dimension of the specified type and name.
	 *
	 * @param type  the type of the dimension, must not be null
	 * @param name  the name of the dimension, must not be null or empty
	 * @param <T>  the type of dimension
	 * @return a dimension, never null
	 */
	static <T> CoordinateAxis<T> of(Class<T> type, String name) {
		return new DefaultCoordinateAxis<>(type, name);
	}
}
