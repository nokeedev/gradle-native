package dev.nokee.internal.testing;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.ConfigurationMatchers.consumable;
import static dev.nokee.internal.testing.utils.ConfigurationTestUtils.testConfiguration;
import static dev.nokee.utils.ConfigurationUtils.configureAsConsumable;

class ConfigurationMatchers_ConsumableTest extends AbstractMatcherTest {
	@Override
	protected Matcher<?> createMatcher() {
		return consumable();
	}

	@Test
	void canCheckMatchingConfiguration() {
		assertMatches(consumable(), testConfiguration(configureAsConsumable()),
			"matches consumable configuration");
	}

	@Test
	void canCheckNonMatchingConfiguration() {
		assertDoesNotMatch(consumable(), testConfiguration(),
			"doesn't match legacy configuration");
	}

	@Test
	void checkDescription() {
		assertDescription("a consumable configuration", consumable());
	}

	@Test
	void checkMismatchDescription() {
		assertMismatchDescription("was a legacy configuration", consumable(), testConfiguration());
	}
}
