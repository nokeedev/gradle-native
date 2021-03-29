package dev.gradleplugins.grava.util;

import com.google.common.testing.NullPointerTester;
import dev.gradleplugins.grava.util.SpecUtils;
import org.junit.jupiter.api.Test;

class SpecUtils_NullTest {
	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkNulls() {
		new NullPointerTester().testAllPublicStaticMethods(SpecUtils.class);
	}
}
