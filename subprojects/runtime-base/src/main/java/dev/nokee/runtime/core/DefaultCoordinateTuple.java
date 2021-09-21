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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import lombok.EqualsAndHashCode;
import lombok.val;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.stream.Collectors.joining;

@EqualsAndHashCode
final class DefaultCoordinateTuple implements CoordinateTuple {
	private final List<Coordinate<?>> list;

	DefaultCoordinateTuple(List<? extends Coordinate<?>> list) {
		checkArgument(!list.isEmpty(), "coordinates cannot be empty");
		checkArgument(!hasDuplicatedAxis(list), "coordinates cannot contains duplicated axis");
		this.list = ImmutableList.copyOf(list);
	}

	private static boolean hasDuplicatedAxis(Iterable<? extends Coordinate<?>> coordinates) {
		val knownAxis = new HashSet<>();
		for (Coordinate<?> coordinate : coordinates) {
			if (!knownAxis.add(coordinate.getAxis())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Iterator<Coordinate<?>> iterator() {
		return list.iterator();
	}

	@Override
	public String toString() {
		return "(" + list.stream().map(it -> it.getValue().toString()).collect(joining(", ")) + ")";
	}
}
