package dev.gradleplugins.grava.util;

import com.google.common.testing.EqualsTester;
import org.gradle.api.specs.Spec;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.grava.util.SpecUtils.satisfyAll;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasToString;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpecUtils_SatisfyAllTest {
	@Test
	void checkToString() {
		assertThat(satisfyAll(), hasToString("SpecUtils.satisfyAll()"));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(satisfyAll(), satisfyAll())
			.addEqualityGroup((Spec<Object>) t -> false)
			.testEquals();
	}

	@Test
	void alwaysReturnTrue() {
		assertAll(() -> {
			assertTrue(satisfyAll().isSatisfiedBy("foo"));
			assertTrue(satisfyAll().isSatisfiedBy(42));
			assertTrue(satisfyAll().isSatisfiedBy(new Object()));
		});
	}
}
