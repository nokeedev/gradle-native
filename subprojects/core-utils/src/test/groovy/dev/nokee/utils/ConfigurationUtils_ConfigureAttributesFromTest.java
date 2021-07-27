package dev.nokee.utils;

import com.google.common.testing.EqualsTester;
import lombok.EqualsAndHashCode;
import lombok.val;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.attributes.Attribute;
import org.gradle.api.attributes.AttributeContainer;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.ConfigurationMatchers.attributes;
import static dev.nokee.internal.testing.utils.ConfigurationTestUtils.testConfiguration;
import static dev.nokee.utils.ConfigurationUtils.*;
import static dev.nokee.utils.ConfigurationUtils_ConfigureAttributesFromTest.TestAttributesProvider.TEST_ATTRIBUTE;
import static dev.nokee.utils.ConsumerTestUtils.aConsumer;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConfigurationUtils_ConfigureAttributesFromTest {
	@Test
	void canConfigureAttributeFromProviderOnConsumableConfiguration() {
		val subject = testConfiguration(configureAsConsumable().andThen(configureAttributesFrom(new TestAttributesProvider())));
		ConfigurationUtils.<Configuration>configureAttributesFrom(new TestAttributesProvider()).execute(subject);
		assertThat(subject, attributes(hasEntry(TEST_ATTRIBUTE, "consumer-value")));
	}

	@Test
	void canConfigureAttributeFromProviderOnResolvableConfiguration() {
		val subject = testConfiguration(configureAsResolvable().andThen(configureAttributesFrom(new TestAttributesProvider())));
		assertThat(subject, attributes(hasEntry(TEST_ATTRIBUTE, "resolver-value")));
	}

	@Test
	void throwsExceptionWhenConfigurationIsNeitherConsumableAndResolvable() {
		val ex = assertThrows(IllegalStateException.class,
			() -> testConfiguration("test", configureAttributesFrom(new TestAttributesProvider())));
		assertThat(ex.getMessage(), equalTo("Configuration 'test' must be either consumable or resolvable."));
	}

	@EqualsAndHashCode
	static final class TestAttributesProvider implements ConfigurationAttributesProvider {
		public static final Attribute<String> TEST_ATTRIBUTE = Attribute.of("test-attribute", String.class);

		@Override
		public void forConsuming(AttributeContainer attributes) {
			attributes.attribute(TEST_ATTRIBUTE, "consumer-value");
		}

		@Override
		public void forResolving(AttributeContainer attributes) {
			attributes.attribute(TEST_ATTRIBUTE, "resolver-value");
		}

		@Override
		public String toString() {
			return "attribute provider";
		}
	}

	@Test
	void returnsEnhanceAction() {
		assertThat(configureAttributes(aConsumer()), isA(ActionUtils.Action.class));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(configureAttributesFrom(new TestAttributesProvider()), configureAttributesFrom(new TestAttributesProvider()))
			.addEqualityGroup(configureAttributesFrom(new ConfigurationAttributesProvider() {
				@Override
				public void forConsuming(AttributeContainer attributes) {}

				@Override
				public void forResolving(AttributeContainer attributes) {}
			}))
			.testEquals();
	}

	@Test
	void checkToString() {
		assertThat(configureAttributesFrom(new TestAttributesProvider()),
			hasToString("ConfigurationUtils.configureAttributesFrom(attribute provider)"));
	}
}
