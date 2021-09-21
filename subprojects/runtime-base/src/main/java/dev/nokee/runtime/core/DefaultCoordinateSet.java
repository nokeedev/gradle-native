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

import com.google.common.collect.ImmutableSet;
import lombok.EqualsAndHashCode;
import lombok.val;

import java.util.Iterator;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;

@EqualsAndHashCode
final class DefaultCoordinateSet<T> implements CoordinateSet<T> {
	private final Set<Coordinate<T>> coordinates;

	public DefaultCoordinateSet(Set<? extends Coordinate<T>> coordinates) {
		checkArgument(!coordinates.isEmpty(), "coordinate set cannot be empty");
		checkArgument(hasSameAxis(coordinates), "all coordinate in a set has to be for the same axis");
		this.coordinates = ImmutableSet.copyOf(coordinates);
	}

	private static boolean hasSameAxis(Iterable<? extends Coordinate<?>> coordinates) {
		val iter = coordinates.iterator();
		val firstCoordinate = iter.next();
		while (iter.hasNext()) {
			val coordinate = iter.next();
			if (!coordinate.getAxis().equals(firstCoordinate.getAxis())) {
				return false;
			}
		}
		return true;
	}

	@Override
	public Iterator<Coordinate<T>> iterator() {
		return coordinates.iterator();
	}
}
