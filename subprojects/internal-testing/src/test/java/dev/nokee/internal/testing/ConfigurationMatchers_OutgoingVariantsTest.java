package dev.nokee.internal.testing;

import lombok.val;
import org.gradle.api.artifacts.Configuration;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.ConfigurationMatchers.outgoingVariants;
import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static dev.nokee.internal.testing.utils.ConfigurationTestUtils.testConfiguration;
import static org.hamcrest.Matchers.hasItem;

class ConfigurationMatchers_OutgoingVariantsTest extends AbstractMatcherTest {
	@Override
	protected Matcher<?> createMatcher() {
		return outgoingVariants(hasItem(named("foo")));
	}

	private static Configuration aConfigurationWithOutgoingVariants() {
		val result = testConfiguration();
		result.getOutgoing().getVariants().create("existing");
		return result;
	}

	@Test
	void canCheckMatchingOutgoingVariants() {
		assertMatches(outgoingVariants(hasItem(named("existing"))), aConfigurationWithOutgoingVariants(),
			"matches configuration with outgoing variant named 'existing'");
	}

	@Test
	void canCheckNonMatchingOutgoingVariants() {
		assertDoesNotMatch(outgoingVariants(hasItem(named("absent"))), aConfigurationWithOutgoingVariants(),
			"doesn't match configuration with outgoing variant named 'absent'");
	}

	@Test
	void checkDescription() {
		assertDescription("configuration's outgoing variants is a collection containing an object named \"foo\"",
			outgoingVariants(hasItem(named("foo"))));
	}

	@Test
	void checkMismatchDescription() {
		assertMismatchDescription("configuration's outgoing variants mismatches were: [the object's name was \"existing\"]",
			outgoingVariants(hasItem(named("foo"))), aConfigurationWithOutgoingVariants());
	}
}
