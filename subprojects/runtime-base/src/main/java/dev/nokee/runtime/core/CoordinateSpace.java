package dev.nokee.runtime.core;

import java.util.stream.Stream;

public interface CoordinateSpace /*extends Iterable<CoordinateTuple>*/ {
//	CoordinateTuple create()

	Stream<CoordinateTuple> getPoints();

//	// The cardinality
//	long getDimension();

	static CoordinateSpace cartesianProduct(CoordinateSet<?>... coordinateSets) {
		throw new UnsupportedOperationException();
	}
//	static Stream<CoordinateTuple> cartesianProduct(Iterable<DimensionSet<?>> dimensionSets) {
//		return Sets.cartesianProduct(Streams.stream(dimensionSets).map(ImmutableSet::copyOf).collect(Collectors.toList())).stream().map(it -> {
//			val builder = CoordinateTuple.builder();
//			int i = 0;
//			for (DimensionSet<?> dimensionSet : dimensionSets) {
//				builder.dimension(dimensionSet.getType(), it.get(i));
//				i++;
//			}
//			return builder.build();
//		});
//	}
}
