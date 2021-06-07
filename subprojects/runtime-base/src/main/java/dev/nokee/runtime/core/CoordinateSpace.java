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
