package dev.nokee.runtime.core;

import static dev.nokee.runtime.core.CoordinateTestUtils.testAxis;

class CoordinateFactory_OfFactoryMethodTest implements CoordinateTester<TestAxis>, CoordinateFactoryTester {
	@Override
	public Coordinate<TestAxis> createSubject() {
		return Coordinate.of(testAxis(), TestAxis.INSTANCE);
	}

	@Override
	public <T> Coordinate<T> createSubject(CoordinateAxis<T> axis, T value) {
		return Coordinate.of(axis, value);
	}
}
