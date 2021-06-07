package dev.nokee.runtime.core;

public class CoordinateSpaceFactory_CartesianProductSetArrayFactoryMethodTest implements CoordinateSpaceFactoryTester {
	@Override
	public CoordinateSpace createSubject(CoordinateSet<?>... coordinateSets) {
		return CoordinateSpace.cartesianProduct(coordinateSets);
	}
}
