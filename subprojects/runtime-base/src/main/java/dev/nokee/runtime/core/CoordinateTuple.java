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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public interface CoordinateTuple extends Iterable<Coordinate<?>> {
	/**
	 * Returns the coordinate value for the specified axis, if available.
	 *
	 * If nested subspace coordinate needs to be searched, use {@link Coordinates#find(CoordinateTuple, CoordinateAxis, boolean)}.
	 * Else, it's possible to flatten the tuple to remove parent coordinate from the search.
	 *
	 * @param axis  the axis to retrieve the value, must not be null
	 * @param <T>  the axis value type
	 * @return the value for the specified axis, never null
	 */
	default <T> Optional<T> find(CoordinateAxis<T> axis) {
		return Coordinates.find(this, axis, false);
	}

	static CoordinateTuple of(Coordinate<?>... coordinates) {
		return of(Arrays.asList(coordinates));
	}

	static CoordinateTuple of(List<? extends Coordinate<?>> coordinates) {
		return new DefaultCoordinateTuple(coordinates);
	}
}
