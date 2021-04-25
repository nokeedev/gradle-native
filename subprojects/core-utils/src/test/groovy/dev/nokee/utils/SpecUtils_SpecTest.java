package dev.nokee.utils;

import lombok.val;
import org.gradle.api.specs.Spec;
import org.gradle.api.specs.Specs;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mockito;

import static dev.nokee.utils.Cast.uncheckedCastBecauseOfTypeErasure;
import static dev.nokee.utils.SpecUtils.Spec.of;
import static dev.nokee.utils.SpecUtils.satisfyAll;
import static dev.nokee.utils.SpecUtils.satisfyNone;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.only;

class SpecUtils_SpecTest {
	@Test
	void canWrapGradleSpec() {
		assertThat(of(it -> true), isA(SpecUtils.Spec.class));
	}

	@Test
	void delegatesToSourceSpec() {
		assertAll(delegateFor(true), delegateFor(false));
	}

	@Test
	void convertGradleSatisfyAllIntoSpecUtilsSatisfyAll() {
		assertThat(of(Specs.satisfyAll()), equalTo(satisfyAll()));
	}

	@Test
	void convertGradleSatisfyNoneIntoSpecUtilsSatisfyNone() {
		assertThat(of(Specs.satisfyNone()), equalTo(satisfyNone()));
	}

	@Nested
	class FluentOr implements SpecOrTester {
		@Override
		public <T> Spec<T> createOrSpec(SpecUtils.Spec<T> first, Spec<? super T> second) {
			return first.or(second);
		}
	}

	@Nested
	class FluentNegate implements SpecNegateTester {
		@Override
		public <T> SpecUtils.Spec<T> createNegateSpec(SpecUtils.Spec<T> spec) {
			return spec.negate();
		}
	}

	@Nested
	class FluentAnd implements SpecAndTester {
		@Override
		public <T> Spec<T> createAndSpec(SpecUtils.Spec<T> first, Spec<? super T> second) {
			return first.and(second);
		}
	}

	private static Executable delegateFor(boolean v) {
		return () -> {
			Spec<Object> spec = uncheckedCastBecauseOfTypeErasure(Mockito.mock(Spec.class));
			val o = new Object();
			Mockito.when(spec.isSatisfiedBy(o)).thenReturn(v);
			assertThat(of(spec).isSatisfiedBy(o), equalTo(v));
			Mockito.verify(spec, only()).isSatisfiedBy(o);
		};
	}
}
