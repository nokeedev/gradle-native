package dev.nokee.internal;

import com.google.common.testing.NullPointerTester;
import org.junit.jupiter.api.Test;
import spock.lang.Subject;

@Subject(Factories.class)
class FactoriesTest {
	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkNulls() {
		new NullPointerTester().testAllPublicStaticMethods(Factories.class);
	}
}
