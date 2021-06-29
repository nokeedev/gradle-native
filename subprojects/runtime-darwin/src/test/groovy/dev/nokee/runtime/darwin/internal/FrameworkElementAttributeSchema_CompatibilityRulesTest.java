package dev.nokee.runtime.darwin.internal;

import dev.nokee.runtime.base.CompatibilityRulesTester;
import org.gradle.api.attributes.LibraryElements;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.objectFactory;
import static dev.nokee.runtime.darwin.internal.DarwinLibraryElements.FRAMEWORK_BUNDLE;
import static org.gradle.api.attributes.LibraryElements.HEADERS_CPLUSPLUS;

class FrameworkElementAttributeSchema_CompatibilityRulesTest {
	private final CompatibilityRulesTester<LibraryElements> tester = CompatibilityRulesTester.of(new FrameworkElementAttributeSchema.CompatibilityRules(objectFactory().named(LibraryElements.class, HEADERS_CPLUSPLUS), objectFactory().named(LibraryElements.class, FRAMEWORK_BUNDLE)));

	@Test
	void frameworkBundlesAreCompatibleWithHeaders() {
		tester.whenConsuming(HEADERS_CPLUSPLUS).fromProducer(FRAMEWORK_BUNDLE).assertMarkedAsCompatible();
	}
}
