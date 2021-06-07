package dev.nokee.runtime.core;

import com.google.common.collect.ImmutableList;

public class CoordinateSpaceFactory_CartesianProductSetIterableFactoryMethodTest implements CoordinateSpaceFactoryTester {
	@Override
	public CoordinateSpace createSubject(CoordinateSet<?>... coordinateSets) {
		return CoordinateSpace.cartesianProduct(ImmutableList.copyOf(coordinateSets));
	}
}
