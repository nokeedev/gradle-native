package dev.nokee.utils;

import com.google.common.testing.NullPointerTester;
import org.junit.jupiter.api.Test;

class TaskUtils_NullTest {
	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkNull() {
		new NullPointerTester().testAllPublicStaticMethods(TaskUtils.class);
	}
}
