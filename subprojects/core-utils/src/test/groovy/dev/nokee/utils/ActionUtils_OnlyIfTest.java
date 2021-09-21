/*
 * Copyright 2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.nokee.utils;

import com.google.common.testing.EqualsTester;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.specs.Spec;
import org.gradle.api.specs.Specs;
import org.junit.jupiter.api.Test;
import spock.lang.Subject;

import static dev.nokee.internal.testing.ExecuteWith.*;
import static dev.nokee.utils.ActionUtils.doNothing;
import static dev.nokee.utils.ActionUtils.onlyIf;
import static dev.nokee.utils.SpecUtils.satisfyAll;
import static dev.nokee.utils.SpecUtils.satisfyNone;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Subject(ActionUtils.class)
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
		ActionUtils.Action<Object> action = t -> {};
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
