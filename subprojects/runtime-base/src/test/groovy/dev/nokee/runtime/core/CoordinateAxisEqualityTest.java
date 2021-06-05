package dev.nokee.runtime.core;

import com.google.common.testing.EqualsTester;
import org.junit.jupiter.api.Test;

import static dev.nokee.runtime.core.CoordinateAxis.of;

class CoordinateAxisEqualityTest {
	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(of(TestAxis.class), of(TestAxis.class), of(TestAxis.class, "test-axis"))
			.addEqualityGroup(of(TestAxis.class, "foo"))
			.addEqualityGroup(of(TestAxis.class, "bar"))
			.addEqualityGroup(of(Object.class))
			.addEqualityGroup(of(Object.class, "foo"))
			.testEquals();
	}
}
