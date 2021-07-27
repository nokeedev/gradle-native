package dev.nokee.utils;

import com.google.common.testing.EqualsTester;
import lombok.val;
import org.gradle.api.Named;
import org.gradle.api.attributes.Attribute;
import org.gradle.api.attributes.Usage;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.objectFactory;
import static dev.nokee.internal.testing.ConfigurationMatchers.attributes;
import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static dev.nokee.internal.testing.utils.ConfigurationTestUtils.testConfiguration;
import static dev.nokee.utils.ConfigurationUtils.ARTIFACT_TYPE_ATTRIBUTE;
import static dev.nokee.utils.ConfigurationUtils.configureAttributes;
import static dev.nokee.utils.ConsumerTestUtils.aConsumer;
import static dev.nokee.utils.ConsumerTestUtils.anotherConsumer;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

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
		assertThat(configureAttributes(aConsumer()), hasToString("ConfigurationUtils.configureAttributes(aConsumer())"));
	}

	private static final Attribute<String> STRING_ATTRIBUTE = Attribute.of("string-attribute", String.class);
	private static final Attribute<Boolean> BOOLEAN_ATTRIBUTE = Attribute.of("boolean-attribute", Boolean.class);
	private static final Attribute<EnumObject> ENUM_ATTRIBUTE = Attribute.of("enum-attribute", EnumObject.class);
	public enum EnumObject {TEST }
	private static final Attribute<NamedObject> NAMED_ATTRIBUTE = Attribute.of("named-attribute", NamedObject.class);
	public interface NamedObject extends Named {}

	@Test
	void canConfigureStringAttributes() {
		val subject = testConfiguration(configureAttributes(it -> it.attribute(STRING_ATTRIBUTE, "test")));
		assertThat(subject, attributes(hasEntry(STRING_ATTRIBUTE, "test")));
	}

	@Test
	void canConfigureBooleanAttributes() {
		val subject = testConfiguration(configureAttributes(it -> it.attribute(BOOLEAN_ATTRIBUTE, Boolean.TRUE)));
		assertThat(subject, attributes(hasEntry(BOOLEAN_ATTRIBUTE, Boolean.TRUE)));
	}

	@Test
	void canConfigureEnumAttributes() {
		val subject = testConfiguration(configureAttributes(it -> it.attribute(ENUM_ATTRIBUTE, EnumObject.TEST)));
		assertThat(subject, attributes(hasEntry(ENUM_ATTRIBUTE, EnumObject.TEST)));
	}

	@Test
	void canConfigureNamedAttributes() {
		val subject = testConfiguration(configureAttributes(it -> it.attribute(NAMED_ATTRIBUTE, objectFactory().named(NamedObject.class, "test"))));
		assertThat(subject, attributes(hasEntry(is(NAMED_ATTRIBUTE), named("test"))));
	}
}
