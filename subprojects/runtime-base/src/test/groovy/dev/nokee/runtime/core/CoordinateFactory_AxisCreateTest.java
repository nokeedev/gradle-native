package dev.nokee.runtime.core;

import static dev.nokee.runtime.core.CoordinateTestUtils.testAxis;

class CoordinateFactory_AxisCreateTest implements CoordinateTester<TestAxis>, CoordinateFactoryTester {
	@Override
	public Coordinate<TestAxis> createSubject() {
		return testAxis().create(TestAxis.INSTANCE);
	}

	@Override
	public <T> Coordinate<T> createSubject(CoordinateAxis<T> axis, T value) {
		return axis.create(value);
	}
}
