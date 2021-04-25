package dev.nokee.utils;

import com.google.common.testing.EqualsTester;
import org.gradle.api.specs.Spec;
import org.junit.jupiter.api.Test;

import static dev.nokee.utils.SpecUtils.satisfyNone;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasToString;
import static org.junit.jupiter.api.Assertions.*;

class SpecUtils_SatisfyNoneTest {
	@Test
	void checkToString() {
		assertThat(satisfyNone(), hasToString("SpecUtils.satisfyNone()"));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(satisfyNone(), satisfyNone())
			.addEqualityGroup((Spec<Object>) t -> true)
			.testEquals();
	}

	@Test
	void alwaysReturnFalse() {
		assertAll(() -> {
			assertFalse(satisfyNone().isSatisfiedBy("foo"));
			assertFalse(satisfyNone().isSatisfiedBy(42));
			assertFalse(satisfyNone().isSatisfiedBy(new Object()));
		});
	}
}
