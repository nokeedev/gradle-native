package dev.gradleplugins.grava.util;

import org.gradle.api.specs.Specs;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.grava.util.SpecUtils.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class SpecUtils_NegateTest implements SpecNegateTester {
	@Test
	void returnsSatisfyAllWhenNegatingGradleSatisfyNone() {
		assertThat(negate(Specs.satisfyNone()), equalTo(satisfyAll()));
	}

	@Test
	void returnsSatisfyNoneWhenNegatingGradleSatisfyAll() {
		assertThat(negate(Specs.satisfyAll()), equalTo(satisfyNone()));
	}

	@Override
	public <T> dev.gradleplugins.grava.util.SpecUtils.Spec<T> createNegateSpec(SpecUtils.Spec<T> spec) {
		return negate(spec);
	}
}
