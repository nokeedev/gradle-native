package dev.nokee.runtime.core;

import dev.nokee.internal.testing.Assumptions;

class CoordinateSetFactory_OfCoordinateArrayFactoryMethodTest implements CoordinateSetFactoryTester {
	@Override
	public <T extends Enum<T>> CoordinateSet<T> createSubject(Class<T> type) {
		return Assumptions.skipCurrentTestExecution("Testing CoordinateSet.of(Coordinate<T>...)");
	}

	@Override
	public <T> CoordinateSet<T> createSubject(Coordinate<T>... coordinates) {
		return CoordinateSet.of(coordinates);
	}

	@Override
	public <T> CoordinateSet<T> createSubject(CoordinateAxis<T> axis, T... values) {
		return Assumptions.skipCurrentTestExecution("Testing CoordinateSet.of(Coordinate<T>...)");
	}
}
