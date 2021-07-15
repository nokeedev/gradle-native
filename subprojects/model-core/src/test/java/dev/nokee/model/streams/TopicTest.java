package dev.nokee.model.streams;

import com.google.common.testing.NullPointerTester;
import org.junit.jupiter.api.Test;

class TopicTest {
	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkNulls() {
		new NullPointerTester().testAllPublicStaticMethods(Topic.class);
	}
}
