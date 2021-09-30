/*
 * Copyright 2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.nokee.platform.base.internal.dependencies;

import com.google.common.testing.EqualsTester;
import dev.nokee.internal.testing.GradleNamedMatchers;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Named;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.attributes.Attribute;
import org.junit.jupiter.api.Test;
import spock.lang.Subject;

import static dev.nokee.internal.testing.ConfigurationMatchers.attributes;
import static dev.nokee.internal.testing.util.ConfigurationTestUtils.testConfiguration;
import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.objectFactory;
import static dev.nokee.platform.base.internal.dependencies.ProjectConfigurationActions.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Subject(ProjectConfigurationActions.class)
class ProjectConfigurationActions_AttributeTest {
	private static final Attribute<Named> ATTRIBUTE = Attribute.of("com.example.attribute", Named.class);

	@Test
	void canConfigureConfigurationNamedAttribute() {
		assertThat(testConfiguration(using(objectFactory(), attribute(ATTRIBUTE, named("foo")))),
			attributes(hasEntry(equalTo(ATTRIBUTE), GradleNamedMatchers.named("foo"))));
	}

	@Test
	void canConfigureConfigurationObjectAttribute() {
		val myAttribute = Attribute.of(MyAttribute.class);
		val myValue = objectFactory().newInstance(MyAttribute.class);
		assertThat(testConfiguration(using(objectFactory(), attribute(myAttribute, ofInstance(myValue)))), attributes(hasEntry(myAttribute, myValue)));
	}

	interface MyAttribute {}

	@Test
	void canConfigureConfigurationEnumAttribute() {
		val myAttribute = Attribute.of(MyEnum.class);
		assertThat(testConfiguration(using(objectFactory(), attribute(myAttribute, ofInstance(MyEnum.VALUE)))), attributes(hasEntry(myAttribute, MyEnum.VALUE)));
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

	private static Action<Configuration> withAttribute(String value) {
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
