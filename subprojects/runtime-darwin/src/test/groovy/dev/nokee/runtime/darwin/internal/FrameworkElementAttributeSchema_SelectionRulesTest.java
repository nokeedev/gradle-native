package dev.nokee.runtime.darwin.internal;

import dev.nokee.runtime.base.DisambiguationRulesTester;
import org.gradle.api.attributes.LibraryElements;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.objectFactory;
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
