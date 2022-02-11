/*
 * Copyright 2022 the original author or authors.
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
package dev.nokee.internal.testing;

import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static dev.nokee.internal.testing.GradleProviderMatchers.hasNoValue;
import static dev.nokee.internal.testing.util.ProjectTestUtils.objectFactory;
import static dev.nokee.internal.testing.util.ProjectTestUtils.providerFactory;
import static java.util.Arrays.asList;

class GradleProviderMatchers_HasNoValueTest extends AbstractMatcherTest {
	@Override
	protected Matcher<?> createMatcher() {
		return hasNoValue();
	}

	//region Property
	@Test
	void matchesPropertyWithNoValue() {
		assertMatches(hasNoValue(), objectFactory().property(String.class),
			"matches property with no value");
	}

	@Test
	void doesNotMatchPropertyWithValue() {
		assertDoesNotMatch(hasNoValue(), objectFactory().property(String.class).value("foo"),
			"does not match property with value");
	}
	//endregion

	//region SetProperty
	@Test
	void matchesSetPropertyWithNoValue() {
		assertMatches(hasNoValue(), objectFactory().setProperty(String.class).value((Iterable<String>) null),
			"matches set property with no value");
	}

	@Test
	void doesNotMatchEmptySetProperty() {
		assertDoesNotMatch(hasNoValue(), objectFactory().setProperty(String.class),
			"does not match empty set property");
	}

	@Test
	void doesNotMatchSetPropertyWithElements() {
		assertDoesNotMatch(hasNoValue(), objectFactory().setProperty(String.class).value(asList("a0", "a1", "a2")),
			"does not match set property with elements");
	}
	//endregion

	//region ListProperty
	@Test
	void matchesListPropertyWithNoValue() {
		assertMatches(hasNoValue(), objectFactory().listProperty(String.class).value((Iterable<String>) null),
			"matches list property with no value");
	}

	@Test
	void doesNotMatchEmptyListProperty() {
		assertDoesNotMatch(hasNoValue(), objectFactory().listProperty(String.class),
			"does not match empty list property");
	}

	@Test
	void doesNotMatchListPropertyWithElements() {
		assertDoesNotMatch(hasNoValue(), objectFactory().listProperty(String.class).value(asList("a0", "a1", "a2")),
			"does not match list property with elements");
	}
	//endregion

	//region MapProperty
	@Test
	void matchesMapPropertyWithNoValue() {
		assertMatches(hasNoValue(), objectFactory().mapProperty(String.class, Object.class).value((Map<String, ?>) null),
			"matches map property with no value");
	}

	@Test
	void doesNotMatchEmptyMapProperty() {
		assertDoesNotMatch(hasNoValue(), objectFactory().mapProperty(String.class, Object.class).empty(),
			"does not match empty map property");
	}

	@Test
	void doesNotMatchMapPropertyWithElements() {
		assertDoesNotMatch(hasNoValue(), objectFactory().mapProperty(String.class, Object.class)
				.value(new HashMap<String, Object>() {{
					put("a0", "v0");
					put("a1", "v1");
					put("a2", "v2");
				}}),
			"does not match map property with entries");
	}
	//endregion

	//region Provider
	@Test
	void matchesProviderWithNoValue() {
		assertMatches(hasNoValue(), providerFactory().provider(() -> null),
			"matches property with no value");
	}

	@Test
	void doesNotMatchProviderWithValue() {
		assertDoesNotMatch(hasNoValue(), providerFactory().provider(() -> "foo"),
			"does not match property with value");
	}
	//endregion


	//region Provider with mapping chain
	@Test
	void matchesProviderChainWithNoValue() {
		final Provider<String> subject = objectFactory().newInstance(PropertyWithDisplayName.class).getFoo()
			.map(it -> it + "-suffix");
		assertMatches(hasNoValue(), subject, "matches property with no value");
	}

	@Test
	void doesNotMatchProviderChainWithValue() {
		final Provider<String> subject = objectFactory().newInstance(PropertyWithDisplayName.class).getFoo()
			.value("foo")
			.map(it -> it + "-suffix");
		assertDoesNotMatch(hasNoValue(), subject,
			"does not match property with value");
	}

	public interface PropertyWithDisplayName {
		Property<String> getFoo();
	}
	//endregion
}
