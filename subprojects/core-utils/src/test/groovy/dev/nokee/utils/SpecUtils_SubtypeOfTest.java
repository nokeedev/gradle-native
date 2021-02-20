package dev.nokee.utils;

import com.google.common.testing.EqualsTester;
import org.junit.jupiter.api.Test;

import static dev.nokee.utils.SpecUtils.satisfyAll;
import static dev.nokee.utils.SpecUtils.subtypeOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class SpecUtils_SubtypeOfTest {
	@Test
	void returnsTrueWhenClassIsSubtypeOfSpecifiedClass() {
		assertThat(subtypeOf(Number.class).isSatisfiedBy(Long.class), equalTo(true));
	}

	@Test
	void returnsFalseWhenClassIsNotSubtypeOfSpecifiedClass() {
		assertThat(subtypeOf(String.class).isSatisfiedBy(Long.class), equalTo(false));
	}

	@Test
	void returnsEnhanceSpec() {
		assertThat(subtypeOf(String.class), isA(SpecUtils.Spec.class));
	}

	@Test
	void checkToString() {
		assertThat(subtypeOf(String.class), hasToString("SpecUtils.subtypeOf(class java.lang.String)"));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(subtypeOf(String.class), subtypeOf(String.class))
			.addEqualityGroup(subtypeOf(Number.class))
			.addEqualityGroup(subtypeOf(Object.class), satisfyAll())
			.testEquals();
	}
}
