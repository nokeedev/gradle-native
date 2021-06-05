package dev.nokee.runtime.core;

import static dev.nokee.runtime.core.CoordinateTestUtils.yAxis;

class CoordinateTupleFactory_OfCoordinateArrayFactoryMethodTest implements CoordinateTupleFactoryTester, CoordinateTupleTester<CoordinateTuple> {
	@Override
	public CoordinateTuple createSubject(Coordinate<?>... coordinates) {
		return CoordinateTuple.of(coordinates);
	}

	@Override
	public CoordinateTuple createSubject() {
		return CoordinateTuple.of(yAxis().create(42L));
	}
}
