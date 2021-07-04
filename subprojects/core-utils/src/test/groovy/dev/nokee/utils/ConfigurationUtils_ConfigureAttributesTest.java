package dev.nokee.utils;

import com.google.common.testing.EqualsTester;
import lombok.val;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.attributes.Attribute;
import org.gradle.api.attributes.AttributeContainer;
import org.gradle.api.attributes.Usage;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.objectFactory;
import static dev.nokee.internal.testing.ConfigurationMatchers.attributes;
import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static dev.nokee.internal.testing.utils.ConfigurationTestUtils.testConfiguration;
import static dev.nokee.utils.ConfigurationUtils.*;
import static dev.nokee.utils.ConfigurationUtils_ConfigureAttributesTest.TestAttributesProvider.TEST_ATTRIBUTE;
import static dev.nokee.utils.ConsumerTestUtils.aConsumer;
import static dev.nokee.utils.ConsumerTestUtils.anotherConsumer;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConfigurationUtils_ConfigureAttributesTest {
	@Test
	void canConfigureUsageAttributes() {
		val subject = testConfiguration(configureAttributes(it -> it.usage(objectFactory().named(Usage.class, "some-usage"))));
		assertThat(subject, attributes(hasEntry(equalTo(Usage.USAGE_ATTRIBUTE), named("some-usage"))));
	}

	@Test
	void canConfigureArtifactType() {
		val subject = testConfiguration(configureAttributes(it -> it.artifactType("some-artifact-type")));
		assertThat(subject, attributes(hasEntry(ARTIFACT_TYPE_ATTRIBUTE, "some-artifact-type")));
	}

	@Test
	void canConfigureAttributeFromProviderOnConsumableConfiguration() {
		val subject = testConfiguration(asConsumable().andThen(configureAttributes(it -> it.from(new TestAttributesProvider()))));
		ConfigurationUtils.<Configuration>configureAttributes(it -> it.from(new TestAttributesProvider())).execute(subject);
		assertThat(subject, attributes(hasEntry(TEST_ATTRIBUTE, "consumer-value")));
	}

	@Test
	void canConfigureAttributeFromProviderOnResolvableConfiguration() {
		val subject = testConfiguration(asResolvable().andThen(configureAttributes(it -> it.from(new TestAttributesProvider()))));
		assertThat(subject, attributes(hasEntry(TEST_ATTRIBUTE, "resolver-value")));
	}

	@Test
	void throwsExceptionWhenConfigurationIsNeitherConsumableAndResolvable() {
		val ex = assertThrows(IllegalStateException.class,
			() -> testConfiguration("test", configureAttributes(it -> it.from(new TestAttributesProvider()))));
		assertThat(ex.getMessage(), equalTo("Configuration 'test' must be either consumable or resolvable."));
	}

	static final class TestAttributesProvider implements AttributesProvider {
		public static final Attribute<String> TEST_ATTRIBUTE = Attribute.of("test-attribute", String.class);

		@Override
		public void forConsuming(AttributeContainer attributes) {
			attributes.attribute(TEST_ATTRIBUTE, "consumer-value");
		}

		@Override
		public void forResolving(AttributeContainer attributes) {
			attributes.attribute(TEST_ATTRIBUTE, "resolver-value");
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
			.addEqualityGroup(configureAttributes(aConsumer()), configureAttributes(aConsumer()))
			.addEqualityGroup(configureAttributes(anotherConsumer()))
			.testEquals();
	}

	@Test
	void checkToString() {
		assertThat(configureAttributes(aConsumer()), hasToString(startsWith("ConfigurationUtils.configureAttributes(aConsumer())")));
	}
}
