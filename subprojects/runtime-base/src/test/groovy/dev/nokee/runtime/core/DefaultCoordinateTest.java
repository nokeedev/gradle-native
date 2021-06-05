package dev.nokee.runtime.core;

import static dev.nokee.runtime.core.CoordinateTestUtils.testAxis;

class DefaultCoordinateTest implements CoordinateTester<TestAxis>, CoordinateFactoryTester {
	@Override
	public Coordinate<TestAxis> createSubject() {
		return new DefaultCoordinate<>(testAxis(), TestAxis.INSTANCE);
	}

	@Override
	public <T> Coordinate<T> createSubject(CoordinateAxis<T> axis, T value) {
		return new DefaultCoordinate<>(axis, value);
	}

//	@Test
//	void checkToString() {
//		assert
//	}
}
