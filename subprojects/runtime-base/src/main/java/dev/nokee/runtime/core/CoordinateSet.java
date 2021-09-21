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

import java.util.Set;

import static java.util.Arrays.stream;

public interface CoordinateSet<T> extends Iterable<Coordinate<T>> {

	static <E extends Enum<E>> CoordinateSet<E> of(Class<E> type) {
		return stream(type.getEnumConstants()).collect(Coordinates.toCoordinateSet(CoordinateAxis.of(type)));
	}

	static <E extends Enum<E>> CoordinateSet<E> of(CoordinateAxis<E> axis) {
		return stream(axis.getType().getEnumConstants()).collect(Coordinates.toCoordinateSet(axis));
	}

	@SafeVarargs
	static <T> CoordinateSet<T> of(CoordinateAxis<T> axis, T... values) {
		return stream(values).collect(Coordinates.toCoordinateSet(axis));
	}

	static <T> CoordinateSet<T> of(CoordinateAxis<T> axis, Set<T> values) {
		return values.stream().collect(Coordinates.toCoordinateSet(axis));
	}

	@SafeVarargs
	static <T> CoordinateSet<T> of(Coordinate<T>... coordinates) {
		return stream(coordinates).collect(Coordinates.toCoordinateSet());
	}

	static <T> CoordinateSet<T> of(Set<? extends Coordinate<T>> coordinates) {
		return coordinates.stream().collect(Coordinates.toCoordinateSet());
	}

	default CoordinateAxis<T> getAxis() {
		return iterator().next().getAxis();
	}

//	public CoordinateSpace multiply(CoordinateSet<?> set) {
//		throw new UnsupportedOperationException();
//	}

//	public CoordinateSet<?> x(CoordinateSet<?> set) {
//		throw new UnsupportedOperationException();
//	}
//
//	public CoordinateSpace toSpace() {
//		throw new UnsupportedOperationException();
//	}
}
