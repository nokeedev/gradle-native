package dev.nokee.platform.base.internal.dependencies;

import com.google.common.testing.EqualsTester;
import lombok.val;
import org.gradle.api.Named;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.attributes.Attribute;
import org.junit.jupiter.api.Test;
import spock.lang.Subject;

import java.util.function.Consumer;

import static dev.nokee.internal.testing.ConfigurationMatchers.hasAttribute;
import static dev.nokee.internal.testing.utils.ConfigurationTestUtils.testConfiguration;
import static dev.nokee.internal.testing.utils.TestUtils.objectFactory;
import static dev.nokee.platform.base.internal.dependencies.ProjectConfigurationUtils.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasToString;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Subject(ProjectConfigurationUtils.class)
class ProjectConfigurationUtils_AttributeTest {
	private static final Attribute<Named> ATTRIBUTE = Attribute.of("com.example.attribute", Named.class);

	@Test
	void canConfigureConfigurationNamedAttribute() {
		assertThat(testConfiguration(using(objectFactory(), attribute(ATTRIBUTE, named("foo")))),
			hasAttribute(ATTRIBUTE, "foo"));
	}

	@Test
	void canConfigureConfigurationObjectAttribute() {
		val myAttribute = Attribute.of(MyAttribute.class);
		val myValue = objectFactory().newInstance(MyAttribute.class);
		assertThat(testConfiguration(using(objectFactory(), attribute(myAttribute, ofInstance(myValue)))), hasAttribute(myAttribute, myValue));
	}

	interface MyAttribute {}

	@Test
	void canConfigureConfigurationEnumAttribute() {
		val myAttribute = Attribute.of(MyEnum.class);
		assertThat(testConfiguration(using(objectFactory(), attribute(myAttribute, ofInstance(MyEnum.VALUE)))), hasAttribute(myAttribute, MyEnum.VALUE));
	}

	enum MyEnum { VALUE }

	@Test
	void canCheckExistingConfigurationWhenHasNoUsageAttribute() {
		val ex = assertThrows(IllegalStateException.class,
			() -> assertConfigured(testConfiguration(), attribute(ATTRIBUTE, named("foo"))));
		assertThat(ex.getMessage(), equalTo("Cannot reuse existing configuration named 'test' because it does not have attribute 'com.example.attribute' of type 'org.gradle.api.Named'."));
	}

	@Test
	void canCheckExistingConfigurationWhenHasWrongUsageAttribute() {
		val ex = assertThrows(IllegalStateException.class,
			() -> assertConfigured(testConfiguration(withAttribute("some-other-value")), attribute(ATTRIBUTE, named("some-value"))));
		assertThat(ex.getMessage(), equalTo("Cannot reuse existing configuration named 'test' because of an unexpected value for attribute 'com.example.attribute' of type 'org.gradle.api.Named'."));
		assertThat(ex.getCause().getMessage(), equalTo("Unexpected attribute value (expecting: some-value, actual: some-other-value)."));
	}

	private static Consumer<Configuration> withAttribute(String value) {
		return configuration -> configuration.getAttributes().attribute(ATTRIBUTE, objectFactory().named(Named.class, value));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(attribute(ATTRIBUTE, named("foo")), attribute(ATTRIBUTE, named("foo")))
			.addEqualityGroup(attribute(ATTRIBUTE, named("bar")))
			.addEqualityGroup(attribute(Attribute.of(Named.class), named("foo")))
			.addEqualityGroup(attribute(Attribute.of(MyEnum.class), ofInstance(MyEnum.VALUE)))
			.testEquals();
	}

	@Test
	void checkToString() {
		assertThat(attribute(ATTRIBUTE, named("foo")),
			hasToString("ProjectConfigurationUtils.attribute(com.example.attribute, named(foo))"));
		assertThat(attribute(Attribute.of("com.example.my-enum", MyEnum.class), ofInstance(MyEnum.VALUE)),
			hasToString("ProjectConfigurationUtils.attribute(com.example.my-enum, ofInstance(VALUE))"));
	}
}
