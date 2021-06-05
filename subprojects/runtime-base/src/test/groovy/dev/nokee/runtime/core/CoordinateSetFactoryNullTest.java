package dev.nokee.runtime.core;

import com.google.common.testing.NullPointerTester;
import org.junit.jupiter.api.Test;

class CoordinateSetFactoryNullTest {
	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkNulls() {
		new NullPointerTester()
			.testAllPublicStaticMethods(CoordinateSet.class);
	}
}
