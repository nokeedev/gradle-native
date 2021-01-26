package dev.nokee.internal.testing;

import lombok.val;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.attributes.Attribute;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.ConfigurationMatchers.attributes;
import static dev.nokee.internal.testing.utils.ConfigurationTestUtils.testConfiguration;
import static org.hamcrest.Matchers.hasEntry;

class ConfigurationMatchers_AttributesTest extends AbstractMatcherTest {
	@Override
	protected Matcher<?> createMatcher() {
		return attributes(hasEntry(Attribute.of(String.class), "foo"));
	}

	private static Configuration aConfigurationWithAttributes() {
		val result = testConfiguration();
		result.getAttributes().attribute(Attribute.of(String.class), "a0");
		result.getAttributes().attribute(Attribute.of("com.example.attribute", String.class), "a1");
		return result;
	}

	@Test
	void canCheckMatchingAttributes() {
		assertMatches(attributes(hasEntry(Attribute.of(String.class), "a0")), aConfigurationWithAttributes(),
			"matches configuration with String attribute of 'a0'");
	}

	@Test
	void canCheckMatchingNamedAttributes() {
		assertMatches(attributes(hasEntry(Attribute.of("com.example.attribute", String.class), "a1")), aConfigurationWithAttributes(),
			"matches configuration with String attribute named 'com.example.attribute' of 'a1'");
	}

	@Test
	void canCheckNonMatchingAttributes() {
		assertDoesNotMatch(attributes(hasEntry(Attribute.of(String.class), "bar")), aConfigurationWithAttributes(),
			"doesn't match configuration with String attribute of 'bar'");
	}

	@Test
	void checkDescription() {
		assertDescription("a configuration with attribute map containing [<java.lang.String>->\"bar\"]",
			attributes(hasEntry(Attribute.of(String.class), "bar")));
	}

	@Test
	void checkMismatchDescription() {
		assertMismatchDescription("attributes map was [<java.lang.String=a0>, <com.example.attribute=a1>]",
			attributes(hasEntry(Attribute.of(String.class), "bar")), aConfigurationWithAttributes());
	}
}
