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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Set;

import static com.google.common.collect.Streams.stream;
import static java.util.stream.Collectors.toList;

/**
 * Represent a coordinate space containing coordinate tuples.
 */
public interface CoordinateSpace extends Iterable<CoordinateTuple> {
	static CoordinateSpace cartesianProduct(CoordinateSet<?>... coordinateSets) {
		return cartesianProduct(ImmutableList.copyOf(coordinateSets));
	}

	static CoordinateSpace cartesianProduct(Iterable<CoordinateSet<?>> coordinateSets) {
		Set<List<Coordinate<?>>> result = Sets.cartesianProduct(stream(coordinateSets).map(ImmutableSet::copyOf).collect(toList()));
		return new DefaultCoordinateSpace(result.stream().map(CoordinateTuple::of).collect(ImmutableSet.toImmutableSet()));
	}

	static CoordinateSpace of(CoordinateTuple... coordinateTuples) {
		return new DefaultCoordinateSpace(ImmutableSet.copyOf(coordinateTuples));
	}
}
