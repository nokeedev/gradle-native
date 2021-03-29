package dev.gradleplugins.grava.util;

import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.Test;

import static com.google.common.collect.ImmutableList.of;
import static dev.gradleplugins.grava.util.TransformerUtils.constant;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;

class TransformerUtils_ConstantTest {
	@Test
	void ignoresTheInputValue() {
		assertAll(
			() -> assertThat(constant(42).transform(of("a", "b", "c")), is(42)),
			() -> assertThat(constant(42).transform(ImmutableSet.of("a", "b", "c")), is(42)),
			() -> assertThat(constant(42).transform(new Double(4.2)), is(42)),
			() -> assertThat(constant(42).transform("obj"), is(42))
		);
	}

	@Test
	void returnsTheConstantValue() {
		assertAll(
			() -> assertThat(constant(42).transform("dummy"), is(42)),
			() -> assertThat(constant("42").transform("dummy"), is("42")),
			() -> assertThat(constant(of("42")).transform("dummy"), contains("42"))
		);
	}

	@Test
	void checkToString() {
		assertThat(constant(42), hasToString("TransformerUtils.constant(42)"));
	}
}
