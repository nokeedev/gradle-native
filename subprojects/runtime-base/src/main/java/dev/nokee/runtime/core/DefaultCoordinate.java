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

import lombok.EqualsAndHashCode;

import static java.util.Objects.requireNonNull;

@EqualsAndHashCode
final class DefaultCoordinate<T> implements Coordinate<T> {
	private final CoordinateAxis<T> axis;
	private final T value;

	public DefaultCoordinate(CoordinateAxis<T> axis, T value) {
		this.axis = requireNonNull(axis);
		this.value = requireNonNull(value);
	}

	@Override
	public CoordinateAxis<T> getAxis() {
		return axis;
	}

	@Override
	public T getValue() {
		return value;
	}

	@Override
	public String toString() {
		return axis + " '" + value + "'";
	}
}
