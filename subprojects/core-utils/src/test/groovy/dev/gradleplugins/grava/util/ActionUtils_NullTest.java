package dev.gradleplugins.grava.util;

import com.google.common.testing.NullPointerTester;
import org.junit.jupiter.api.Test;

public class ActionUtils_NullTest {
	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkStaticMethodsNullArguments() {
		new NullPointerTester().testAllPublicStaticMethods(ActionUtils.class);
	}
}
