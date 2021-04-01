package dev.gradleplugins.grava.util;

import com.google.common.testing.EqualsTester;
import org.gradle.api.Action;
import org.junit.jupiter.api.Test;
import spock.lang.Subject;

import static dev.gradleplugins.grava.util.ActionUtils.doNothing;
import static dev.gradleplugins.grava.util.ActionUtils.doesSomething;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@Subject(dev.gradleplugins.grava.util.ActionUtils.class)
class ActionUtils_DoNothingTest {
	@Test
	void doesNothingForAnyObjectInput() {
		assertDoesNotThrow(() -> doNothing().execute(new Object()));
	}

	@Test
	void doesNothingForNullInput() {
		assertDoesNotThrow(() -> doNothing().execute(null));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		Action<?> doesSomething = t -> {};
		new EqualsTester()
			.addEqualityGroup(doNothing(), doNothing())
			.addEqualityGroup(doesSomething)
			.testEquals();
	}

	@Test
	void checkToString() {
		assertThat(doNothing(), hasToString("ActionUtils.doNothing()"));
	}

	@Test
	void canCheckIfActionDoesSomething() {
		assertThat(doesSomething(doNothing()), equalTo(false));
		assertThat(doesSomething(t -> {}), equalTo(true));
	}

	@Test
	void returnsEnhanceAction() {
		assertThat(doNothing(), isA(ActionUtils.Action.class));
	}
}
