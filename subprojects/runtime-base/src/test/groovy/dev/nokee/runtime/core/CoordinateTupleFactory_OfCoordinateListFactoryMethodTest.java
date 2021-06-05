package dev.nokee.runtime.core;

import java.util.Arrays;
import java.util.Collections;

import static dev.nokee.runtime.core.CoordinateTestUtils.xAxis;

class CoordinateTupleFactory_OfCoordinateListFactoryMethodTest implements CoordinateTupleFactoryTester, CoordinateTupleTester<CoordinateTuple> {
	@Override
	public CoordinateTuple createSubject(Coordinate<?>... coordinates) {
		return CoordinateTuple.of(Arrays.asList(coordinates));
	}

	@Override
	public CoordinateTuple createSubject() {
		return CoordinateTuple.of(Collections.singletonList(xAxis().create(42L)));
	}
}
