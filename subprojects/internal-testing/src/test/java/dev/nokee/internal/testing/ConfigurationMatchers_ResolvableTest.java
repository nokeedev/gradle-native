package dev.nokee.internal.testing;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.ConfigurationMatchers.resolvable;
import static dev.nokee.internal.testing.utils.ConfigurationTestUtils.testConfiguration;
import static dev.nokee.utils.ConfigurationUtils.asResolvable;

class ConfigurationMatchers_ResolvableTest extends AbstractMatcherTest {
	@Override
	protected Matcher<?> createMatcher() {
		return resolvable();
	}

	@Test
	void canCheckMatchingConfiguration() {
		assertMatches(resolvable(), testConfiguration(asResolvable()),
			"matches resolvable configuration");
	}

	@Test
	void canCheckNonMatchingConfiguration() {
		assertDoesNotMatch(resolvable(), testConfiguration(),
			"doesn't match legacy configuration");
	}

	@Test
	void checkDescription() {
		assertDescription("a resolvable configuration", resolvable());
	}

	@Test
	void checkMismatchDescription() {
		assertMismatchDescription("was a legacy configuration", resolvable(), testConfiguration());
	}
}
