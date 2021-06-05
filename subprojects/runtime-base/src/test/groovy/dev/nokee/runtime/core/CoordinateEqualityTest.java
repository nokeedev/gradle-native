package dev.nokee.runtime.core;

import com.google.common.testing.EqualsTester;
import org.junit.jupiter.api.Test;

import static dev.nokee.runtime.core.CoordinateTestUtils.xAxis;
import static dev.nokee.runtime.core.CoordinateTestUtils.yAxis;

class CoordinateEqualityTest {
	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(Coordinate.of(xAxis(), 1L), Coordinate.of(xAxis(), 1L), xAxis().create(1L))
			.addEqualityGroup(Coordinate.of(yAxis(), 1L), yAxis().create(1L))
			.addEqualityGroup(Coordinate.of(xAxis(), 2L), xAxis().create(2L))
			.testEquals();
	}
}
