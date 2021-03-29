package dev.gradleplugins.grava.util;

import com.google.common.testing.EqualsTester;
import lombok.val;
import org.gradle.api.specs.Spec;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.grava.testing.util.SpecTestUtils.aSpec;
import static dev.gradleplugins.grava.util.SpecUtils.instanceOf;
import static dev.gradleplugins.grava.util.SpecUtils.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class SpecUtils_InstanceOfTest {
	@Test
	void returnsTrueWhenObjectIsASubtype() {
		assertThat(instanceOf(Base.class).isSatisfiedBy(new Child()), equalTo(true));
	}

	@Test
	void returnsTrueWhenObjectIsSameType() {
		assertThat(instanceOf(Base.class).isSatisfiedBy(new Base()), equalTo(true));
	}

	@Test
	void returnsFalseWhenObjectIsNotASubtype() {
		assertThat(instanceOf(Base.class).isSatisfiedBy(new Object()), equalTo(false));
	}

	@Test
	void returnsSatisfyAllWhenCheckingInstanceOfObject() {
		assertThat(instanceOf(Object.class), equalTo(satisfyAll()));
	}

	@Test
	void canChainSpecSafely() {
		assertThat(instanceOf(Base.class, it -> "true".equals(it.base))
				.and(instanceOf(Child.class, it -> it.value == 42))
				.isSatisfiedBy(new Child("true", 42)),
			equalTo(true));
	}

	@Test
	void canChainGradleSpecSafely() {
		assertThat(instanceOf(Base.class, baseIs("true")).isSatisfiedBy(new Child("true", 42)),
			equalTo(true));
	}

	@Test
	void checkToString() {
		assertThat(instanceOf(Base.class),
			hasToString("SpecUtils.instanceOf(class dev.gradleplugins.grava.util.SpecUtils_InstanceOfTest$Base)"));
		assertThat(instanceOf(Base.class, aSpec()),
			hasToString("SpecUtils.and(SpecUtils.instanceOf(class dev.gradleplugins.grava.util.SpecUtils_InstanceOfTest$Base), aSpec())"));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		val andThen = baseIs("true");
		new EqualsTester()
			.addEqualityGroup(instanceOf(Base.class), instanceOf(Base.class))
			.addEqualityGroup(instanceOf(Child.class))
			.addEqualityGroup(instanceOf(Base.class, andThen), instanceOf(Base.class, andThen))
			.addEqualityGroup(instanceOf(Base.class, baseIs("false")))
			.testEquals();
	}

	private static Spec<Base> baseIs(String value) {
		return t -> t.base.equals(value);
	}

	private static class Base {
		private final String base;

		Base() {
			this("");
		}

		Base(String base) {
			this.base = base;
		}
	}
	private static class Child extends Base {
		private final int value;

		Child() {
			this("", 0);
		}

		Child(String base, int value) {
			super(base);
			this.value = value;
		}
	}
}
