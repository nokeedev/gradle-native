package dev.nokee.internal.testing;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.ConfigurationMatchers.declarable;
import static dev.nokee.internal.testing.utils.ConfigurationTestUtils.testConfiguration;
import static dev.nokee.utils.ConfigurationUtils.configureAsDeclarable;

class ConfigurationMatchers_DeclarableTest extends AbstractMatcherTest {
	@Override
	protected Matcher<?> createMatcher() {
		return declarable();
	}

	@Test
	void canCheckMatchingConfiguration() {
		assertMatches(declarable(), testConfiguration(configureAsDeclarable()),
			"matches declarable configuration");
	}

	@Test
	void canCheckNonMatchingConfiguration() {
		assertDoesNotMatch(declarable(), testConfiguration(),
			"doesn't match legacy configuration");
	}

	@Test
	void checkDescription() {
		assertDescription("a declarable configuration", declarable());
	}

	@Test
	void checkMismatchDescription() {
		assertMismatchDescription("was a legacy configuration", declarable(), testConfiguration());
	}
}
