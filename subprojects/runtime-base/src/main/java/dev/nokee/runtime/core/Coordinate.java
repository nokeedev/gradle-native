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
 * Represent a value with its associated axis coordinate.
 * Multiple coordinate forms a coordinate tuple.
 *
 * @param <T>  the value type
 */
public interface Coordinate<T> {
	/**
	 * Returns the axis for this value.
	 *
	 * @return the axis, never null
	 */
	default CoordinateAxis<T> getAxis() {
		return Coordinates.inferCoordinateAxisFromCoordinateImplementation(this);
	}

	/**
	 * Returns the coordinate value.
	 *
	 * @return the value, never null
	 */
	default T getValue() {
		return Coordinates.inferCoordinateValueFromCoordinateImplementation(this);
	}

	/**
	 * Creates a coordinate for the specified axis and value.
	 *
	 * @param axis  the axis for the value, must not be null
	 * @param value  the coordinate value, must not be null
	 * @param <T>  the value type
	 * @return a coordinate, never null
	 */
	static <T> Coordinate<T> of(CoordinateAxis<T> axis, T value) {
		return new DefaultCoordinate<>(axis, value);
	}
}
