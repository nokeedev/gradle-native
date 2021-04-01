package dev.gradleplugins.grava.util;

import com.google.common.testing.EqualsTester;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.specs.Spec;
import org.gradle.api.specs.Specs;
import org.junit.jupiter.api.Test;
import spock.lang.Subject;

import static dev.gradleplugins.grava.util.ActionUtils.doNothing;
import static dev.gradleplugins.grava.util.ActionUtils.onlyIf;
import static dev.gradleplugins.grava.util.SpecUtils.satisfyAll;
import static dev.gradleplugins.grava.util.SpecUtils.satisfyNone;
import static dev.nokee.internal.testing.ExecuteWith.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Subject(dev.gradleplugins.grava.util.ActionUtils.class)
class ActionUtils_OnlyIfTest {
	@Test
	void executesActionIfSpecIsSatisfied() {
		assertThat(executeWith(action(action -> onlyIf(satisfyAll(), action).execute("foo"))),
			calledOnceWith("foo"));
	}

	@Test
	void doesNotExecutesActionIfSpecIsNotSatisfied() {
		assertThat(executeWith(action(action -> onlyIf(satisfyNone(), action).execute("bar"))), neverCalled());
	}

	@Test
	void returnsDoNothingActionForObviousSatisfyNoneSpec() {
		assertThat(onlyIf(satisfyNone(), t -> {}), equalTo(doNothing()));
		assertThat(onlyIf(Specs.satisfyNone(), t -> {}), equalTo(doNothing()));
	}

	@Test
	void returnsSpecifiedActionForObviousSatisfyAllSpec() {
		dev.gradleplugins.grava.util.ActionUtils.Action<Object> action = t -> {};
		assertThat(onlyIf(satisfyAll(), action), equalTo(action));
		assertThat(onlyIf(Specs.satisfyAll(), action), equalTo(action));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		Spec<Object> spec = t -> true;
		Spec<Object> anotherSpec = t -> false;
		Action<Object> doSomething = t -> {};
		Action<Object> doSomethingElse = t -> {};
		new EqualsTester()
			.addEqualityGroup(onlyIf(spec, doSomething), onlyIf(spec, doSomething))
			.addEqualityGroup(onlyIf(anotherSpec, doSomething))
			.addEqualityGroup(onlyIf(spec, doSomethingElse))
			.testEquals();
	}

	@Test
	void checkToString() {
		val spec = new Spec<Object>() {
			@Override
			public boolean isSatisfiedBy(Object o) {
				return true;
			}

			@Override
			public String toString() {
				return "spec";
			}
		};
		val doSomething = new Action<Object>() {
			@Override
			public void execute(Object o) {}

			@Override
			public String toString() {
				return "action";
			}
		};
		assertThat(onlyIf(spec, doSomething), hasToString("ActionUtils.onlyIf(spec, action)"));
	}

	@Test
	void returnsEnhanceAction() {
		assertThat(onlyIf(t -> true, t -> {}), isA(ActionUtils.Action.class));
	}
}
