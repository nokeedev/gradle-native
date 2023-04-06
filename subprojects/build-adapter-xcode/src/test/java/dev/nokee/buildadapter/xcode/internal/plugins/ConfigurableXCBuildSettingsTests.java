/*
 * Copyright 2023 the original author or authors.
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
package dev.nokee.buildadapter.xcode.internal.plugins;

import com.google.common.collect.Iterators;
import dev.nokee.xcode.DefaultXCBuildSettingLayer;
import dev.nokee.xcode.XCBuildSettingLayer;
import dev.nokee.xcode.XCBuildSettingLiteral;
import org.gradle.api.provider.MapProperty;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.google.common.collect.ImmutableMap.of;
import static dev.nokee.internal.testing.util.ProjectTestUtils.objectFactory;
import static dev.nokee.utils.DeferUtils.asToStringObject;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

class ConfigurableXCBuildSettingsTests {
	ConfigurableXCBuildSettings subject = objectFactory().newInstance(ConfigurableXCBuildSettings.class);

	interface MapValueTester {
		void methodUnderTest(Object obj);

		ConfigurableXCBuildSettings subject();

		@Test
		default void canAddStringKeyAndStringValue() {
			methodUnderTest(of("MY_KEY1", "value1"));
			assertThat(subject().get("MY_KEY1"), equalTo("value1"));
		}

		@Test
		default void canAddStringKeyAndBoxedPrimitiveValue() {
			methodUnderTest(of("MY_KEY2", 42));
			assertThat(subject().get("MY_KEY2"), equalTo("42"));
		}

		@Test
		default void canAddStringKeyAndToStringableObjectValue() {
			methodUnderTest(of("MY_KEY3", asToStringObject("value3")));
			assertThat("convert using Object#toString",
				subject().get("MY_KEY3"), equalTo("value3"));
		}

		@Test
		default void canAddStringKeyAndIterableOfStringValue() {
			methodUnderTest(of("MY_KEY4", iterableOf("value4a", "value4b")));
			assertThat("converts to space separated list",
				subject().get("MY_KEY4"), equalTo("value4a value4b"));
		}

		@Test
		default void canAddStringKeyAndIterableOfBoxedPrimitiveValue() {
			methodUnderTest(of("MY_KEY5", iterableOf(1, 2, 3)));
			assertThat("converts to space separated list",
				subject().get("MY_KEY5"), equalTo("1 2 3"));
		}

		@Test
		default void canAddStringKeyAndIterableOfToStringableObjectValue() {
			methodUnderTest(of("MY_KEY6", iterableOf(asToStringObject("value6a"), asToStringObject("value6b"))));
			assertThat("converts to space separated list and Object#toString",
				subject().get("MY_KEY6"), equalTo("value6a value6b"));
		}
	}


	@Nested
	class From implements MapValueTester {
		@Override
		public void methodUnderTest(Object obj) {
			subject.from(obj);
		}

		@Override
		public ConfigurableXCBuildSettings subject() {
			return subject;
		}

		@Test
		void canOverrideAlreadyDefinedBuildSetting() {
			subject.from(of("MY_KEY7", "first"));
			subject.from(of("MY_KEY7", "second"));
			assertThat(subject.get("MY_KEY7"), equalTo("second"));
		}
	}

	@Nested
	class SetFrom implements MapValueTester {
		@Override
		public void methodUnderTest(Object obj) {
			subject.setFrom(obj);
		}

		@Override
		public ConfigurableXCBuildSettings subject() {
			return subject;
		}
	}

	@Test
	void canAddBuildSettingsReferencingOtherBuildSettings() {
		subject.from(of("MY_VAR", "$MY_OTHER_VAR", "MY_OTHER_VAR", "other-value"));
		assertThat(subject.get("MY_VAR"), equalTo("other-value"));
	}

	@Test
	void canAddBuildSettingsViaMutableProvidedMap() {
		MapProperty<String, String> property = objectFactory().mapProperty(String.class, String.class);
		subject.from(property);
		assertThat(subject.get("MY_VAR"), nullValue());

		property.put("MY_VAR", "this-value");
		assertThat(subject.get("MY_VAR"), equalTo("this-value"));

		property.put("MY_VAR", "this-overridden-value");
		assertThat(subject.get("MY_VAR"), equalTo("this-overridden-value"));
	}

	@Test
	void canAddBuildSettingsViaBuildSettingLayer() {
		XCBuildSettingLayer layer = new DefaultXCBuildSettingLayer(of("MY_VAR", new XCBuildSettingLiteral("MY_VAR", "literal-value")));
		subject.from(layer);
		assertThat(subject.get("MY_VAR"), equalTo("literal-value"));
	}

	@SafeVarargs
	@SuppressWarnings("varargs")
	private static <T> Iterable<T> iterableOf(T... values) {
		return () -> Iterators.forArray(values);
	}
}
