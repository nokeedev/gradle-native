package dev.nokee.internal;

import com.google.common.testing.EqualsTester;
import lombok.val;
import org.junit.jupiter.api.Test;
import spock.lang.Subject;

import static dev.nokee.internal.Factories.constant;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasToString;

@Subject(Factories.class)
class Factories_ConstantTest {
	@Test
	void alwaysReturnTheSpecifiedConstant() {
		val factory = constant(42);
		assertThat(factory.create(), equalTo(42));
		assertThat(factory.create(), equalTo(42));
		assertThat(factory.create(), equalTo(42));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(constant(42), constant(42))
			.addEqualityGroup(constant(24))
			.addEqualityGroup(constant("foo"))
			.testEquals();
	}

	@Test
	void checkToString() {
		assertThat(constant(42), hasToString("Factories.constant(42)"));
	}
}
