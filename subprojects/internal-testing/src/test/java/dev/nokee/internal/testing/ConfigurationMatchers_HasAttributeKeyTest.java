package dev.nokee.internal.testing;

import lombok.val;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.attributes.Attribute;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.ConfigurationMatchers.hasAttribute;
import static dev.nokee.internal.testing.utils.ConfigurationTestUtils.testConfiguration;
import static org.hamcrest.Matchers.equalTo;

class ConfigurationMatchers_HasAttributeKeyTest extends AbstractMatcherTest {
	@Override
	protected Matcher<?> createMatcher() {
		return hasAttribute(equalTo(Attribute.of(String.class)), equalTo("foo"));
	}

	private static Configuration aConfigurationWithAttributes() {
		val result = testConfiguration();
		result.getAttributes().attribute(Attribute.of(String.class), "a0");
		result.getAttributes().attribute(Attribute.of("com.example.attribute", String.class), "a1");
		return result;
	}

	@Test
	void canCheckMatchingAttributes() {
		assertMatches(hasAttribute(Attribute.of(String.class)), aConfigurationWithAttributes().getAttributes(),
			"matches configuration with String attribute of 'a0'");
	}

	@Test
	void canCheckMatchingNamedAttributes() {
		assertMatches(hasAttribute(Attribute.of("com.example.attribute", String.class)), aConfigurationWithAttributes().getAttributes(),
			"matches configuration with String attribute named 'com.example.attribute' of 'a1'");
	}

	@Test
	void canCheckNonMatchingAttributes() {
		assertDoesNotMatch(hasAttribute(Attribute.of(Integer.class)), aConfigurationWithAttributes().getAttributes(),
			"doesn't match configuration with String attribute of 'bar'");
	}

	@Test
	void checkDescription() {
		assertDescription("an attribute map containing [<java.lang.String>->ANYTHING]",
			hasAttribute(Attribute.of(String.class)));
	}

	@Test
	void checkMismatchDescription() {
		assertMismatchDescription("the attribute map was [<java.lang.String=a0>, <com.example.attribute=a1>]",
			hasAttribute(Attribute.of(Integer.class)), aConfigurationWithAttributes().getAttributes());
	}
}
