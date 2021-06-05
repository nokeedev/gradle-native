package dev.nokee.runtime.core;

import dev.nokee.internal.testing.Assumptions;

class CoordinateSetFactory_OfEnumAxisFactoryMethodTest implements CoordinateSetFactoryTester {
	@Override
	public <T extends Enum<T>> CoordinateSet<T> createSubject(Class<T> type) {
		return CoordinateSet.of(CoordinateAxis.of(type));
	}

	@Override
	public <T> CoordinateSet<T> createSubject(Coordinate<T>... coordinates) {
		return Assumptions.skipCurrentTestExecution("Testing CoordinateSet.of(CoordinateAxis<T>)");
	}

	@Override
	public <T> CoordinateSet<T> createSubject(CoordinateAxis<T> axis, T... values) {
		return Assumptions.skipCurrentTestExecution("Testing CoordinateSet.of(CoordinateAxis<T>)");
	}
}
