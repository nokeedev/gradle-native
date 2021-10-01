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
package dev.nokee.runtime.darwin.internal;

import dev.nokee.runtime.base.DisambiguationRulesTester;
import org.gradle.api.attributes.LibraryElements;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.util.ProjectTestUtils.objectFactory;
import static dev.nokee.runtime.darwin.internal.DarwinLibraryElements.FRAMEWORK_BUNDLE;

class FrameworkElementAttributeSchema_SelectionRulesTest {
	private final DisambiguationRulesTester<LibraryElements> tester = DisambiguationRulesTester.of(new FrameworkElementAttributeSchema.SelectionRules(objectFactory().named(LibraryElements.class, LibraryElements.HEADERS_CPLUSPLUS), objectFactory().named(LibraryElements.class, FRAMEWORK_BUNDLE)));

	@Test
	void selectRequestedFrameworkBundleIfAvailable() {
		tester.whenConsuming(FRAMEWORK_BUNDLE)
			.fromCandidates(LibraryElements.HEADERS_CPLUSPLUS, FRAMEWORK_BUNDLE)
			.assertClosestMatch(FRAMEWORK_BUNDLE);
	}

	@Test
	void selectRequestedHeadersIfAvailable() {
		tester.whenConsuming(LibraryElements.HEADERS_CPLUSPLUS)
			.fromCandidates(LibraryElements.HEADERS_CPLUSPLUS, FRAMEWORK_BUNDLE)
			.assertClosestMatch(LibraryElements.HEADERS_CPLUSPLUS);
	}

	@Test
	void preferHeadersWhenNoneRequested() {
		tester.whenConsuming(null)
			.fromCandidates(LibraryElements.HEADERS_CPLUSPLUS, FRAMEWORK_BUNDLE)
			.assertClosestMatch(LibraryElements.HEADERS_CPLUSPLUS);
	}

	@Test
	void selectFrameworkWhenNoneRequestedAndOnlyFrameworkAvailable() {
		tester.whenConsuming(null)
			.fromCandidates(FRAMEWORK_BUNDLE)
			.assertClosestMatch(FRAMEWORK_BUNDLE);
	}

	@Test
	void doesNotSelectFrameworkWhenRequestingHeadersButOnlyFrameworkAvailable() {
		tester.whenConsuming(LibraryElements.HEADERS_CPLUSPLUS)
			.fromCandidates(FRAMEWORK_BUNDLE)
			.assertClosestMatch(FRAMEWORK_BUNDLE);
	}
}
