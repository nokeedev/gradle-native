package dev.nokee.runtime.core;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static com.google.common.collect.ImmutableList.of;
import static dev.nokee.runtime.core.CoordinateTestUtils.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasToString;
import static org.junit.jupiter.api.Assertions.assertAll;

class DefaultCoordinateTupleTest implements CoordinateTupleTester<DefaultCoordinateTuple>, CoordinateTupleFactoryTester {
	@Override
	public DefaultCoordinateTuple createSubject() {
		return new DefaultCoordinateTuple(of(xAxis().create(5L), yAxis().create(2L)));
	}

	@Override
	public DefaultCoordinateTuple createSubject(Coordinate<?>... coordinates) {
		return new DefaultCoordinateTuple(Arrays.asList(coordinates));
	}

	@Test
	void checkToString() {
		assertAll(
			() -> assertThat(new DefaultCoordinateTuple(of(xAxis().create(2L))),
				hasToString("(2)")),
			() -> assertThat(new DefaultCoordinateTuple(of(xAxis().create(4L), yAxis().create(2L))),
				hasToString("(4, 2)")),
			() -> assertThat(new DefaultCoordinateTuple(of(xAxis().create(3L), yAxis().create(6L), zAxis().create(0L))),
				hasToString("(3, 6, 0)"))
		);
	}
}
