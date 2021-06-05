package dev.nokee.runtime.core;

import com.google.common.collect.ImmutableSet;
import com.google.common.testing.EqualsTester;
import org.junit.jupiter.api.Test;

import static dev.nokee.runtime.core.CoordinateSet.of;
import static dev.nokee.runtime.core.CoordinateTestUtils.xAxis;
import static dev.nokee.runtime.core.CoordinateTestUtils.yAxis;

class CoordinateSetEqualityTest {
	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(of(xAxis(), 1L), of(xAxis(), 1L))
			.addEqualityGroup(of(xAxis(), 1L, 2L, 3L), of(xAxis(), 3L, 1L, 2L), of(xAxis().create(2L), xAxis().create(3L), xAxis().create(1L)), of(ImmutableSet.of(xAxis().create(3L), xAxis().create(2L), xAxis().create(1L))), of(xAxis(), ImmutableSet.of(1L, 2L, 3L)))
			.addEqualityGroup(of(yAxis(), 1L))
			.addEqualityGroup(of(Axis.class), of(AXIS), of(AXIS, Axis.values()),
				of(AXIS.create(Axis.a), AXIS.create(Axis.b), AXIS.create(Axis.c)),
				of(ImmutableSet.of(AXIS.create(Axis.a), AXIS.create(Axis.b), AXIS.create(Axis.c))))
			.testEquals();
	}

	CoordinateAxis<Axis> AXIS = CoordinateAxis.of(Axis.class);
	enum Axis { a, b, c }
}
