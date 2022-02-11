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

import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.GradleProviderMatchers.changesDisallowed;
import static dev.nokee.internal.testing.GradleProviderMatchers.finalizedValue;
import static dev.nokee.internal.testing.util.ProjectTestUtils.objectFactory;
import static dev.nokee.utils.ProviderUtils.disallowChanges;
import static dev.nokee.utils.ProviderUtils.finalizeValue;

class GradleProviderMatchers_ChangesDisallowedTest extends AbstractMatcherTest {
	@Override
	protected Matcher<?> createMatcher() {
		return changesDisallowed();
	}

	//region Property
	@Test
	void matchesFinalizedProperty() {
		assertMatches(changesDisallowed(), finalizeValue(objectFactory().property(String.class)),
			"matches finalized property");
	}

	@Test
	void doesNotMatchMutableProperty() {
		assertDoesNotMatch(changesDisallowed(), objectFactory().property(String.class),
			"does not match mutable property");
	}

	@Test
	void matchesImmutableProperty() {
		assertMatches(changesDisallowed(), disallowChanges(objectFactory().property(String.class)),
			"matches immutable property");
	}
	//endregion

	//region SetProperty
	@Test
	void matchesFinalizedSetProperty() {
		assertMatches(changesDisallowed(), finalizeValue(objectFactory().setProperty(String.class)),
			"matches finalized set property");
	}

	@Test
	void doesNotMatchMutablelSetProperty() {
		assertDoesNotMatch(changesDisallowed(), objectFactory().setProperty(String.class),
			"does not match mutable set property");
	}

	@Test
	void matchesImmutableSetProperty() {
		assertMatches(changesDisallowed(), disallowChanges(objectFactory().setProperty(String.class)),
			"matches immutable set property");
	}
	//endregion

	//region ListProperty
	@Test
	void matchesFinalizedListProperty() {
		assertMatches(changesDisallowed(), finalizeValue(objectFactory().listProperty(String.class)),
			"matches finalized list property");
	}

	@Test
	void doesNotMatchMutableListProperty() {
		assertDoesNotMatch(changesDisallowed(), objectFactory().listProperty(String.class),
			"does not match mutable list property");
	}

	@Test
	void matchesImmutableListProperty() {
		assertMatches(changesDisallowed(), disallowChanges(objectFactory().listProperty(String.class)),
			"matches immutable list property");
	}
	//endregion

	//region MapProperty
	@Test
	void matchesFinalizedMapProperty() {
		assertMatches(changesDisallowed(), finalizeValue(objectFactory().mapProperty(String.class, Object.class)),
			"matches finalized map property");
	}

	@Test
	void doesNotMatchMutableMapProperty() {
		assertDoesNotMatch(finalizedValue(), objectFactory().mapProperty(String.class, Object.class),
			"does not match mutable map property");
	}

	@Test
	void matchesImmutableMapProperty() {
		assertMatches(changesDisallowed(), disallowChanges(objectFactory().mapProperty(String.class, Object.class)),
			"matches immutable map property");
	}
	//endregion

	//region ConfigurableFileCollection
	@Test
	void matchesFinalizedFileCollection() {
		assertMatches(changesDisallowed(), finalizeValue(objectFactory().fileCollection()),
			"matches finalized file collection");
	}

	@Test
	void doesNotMatchMutableFileCollection() {
		assertDoesNotMatch(changesDisallowed(), objectFactory().fileCollection(),
			"does not match mutable file collection");
	}

	@Test
	void matchesImmutableFileCollection() {
		assertMatches(changesDisallowed(), disallowChanges(objectFactory().fileCollection()),
			"matches immutable file collection");
	}
	//endregion
}
