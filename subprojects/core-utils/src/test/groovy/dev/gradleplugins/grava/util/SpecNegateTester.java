package dev.gradleplugins.grava.util;

import com.google.common.testing.EqualsTester;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.grava.testing.util.SpecTestUtils.aSpec;
import static dev.gradleplugins.grava.testing.util.SpecTestUtils.anotherSpec;
import static dev.gradleplugins.grava.util.SpecUtils.*;
import static dev.gradleplugins.grava.util.SpecUtils.satisfyNone;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasToString;

public interface SpecNegateTester {
	Object IN = new Object();

	<T> dev.gradleplugins.grava.util.SpecUtils.Spec<T> createNegateSpec(SpecUtils.Spec<T> spec);

	@Test
	default void returnsSourceSpecWhenNegatingTwice() {
		assertThat(createNegateSpec(createNegateSpec(aSpec())), equalTo(aSpec()));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	default void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(createNegateSpec(aSpec()), createNegateSpec(aSpec()))
			.addEqualityGroup(createNegateSpec(anotherSpec()))
			.testEquals();
	}

	@Test
	default void canNegateSatisfyingSpec() {
		assertThat(createNegateSpec(it -> true).isSatisfiedBy(IN), equalTo(false));
	}

	@Test
	default void canNegateUnsatisfyingSpec() {
		assertThat(createNegateSpec(it -> false).isSatisfiedBy(IN), equalTo(true));
	}

	@Test
	default void checkToString() {
		assertThat(createNegateSpec(aSpec()), hasToString("SpecUtils.negate(aSpec())"));
	}

	@Test
	default void returnsSatisfyAllWhenNegatingSatisfyNone() {
		assertThat(createNegateSpec(satisfyNone()), equalTo(satisfyAll()));
	}

	@Test
	default void returnsSatisfyNoneWhenNegatingSatisfyAll() {
		assertThat(createNegateSpec(satisfyAll()), equalTo(satisfyNone()));
	}
}
