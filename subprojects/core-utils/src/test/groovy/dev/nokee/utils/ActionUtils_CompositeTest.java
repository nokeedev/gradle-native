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

import lombok.val;
import org.gradle.api.Action;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.ImmutableList.of;
import static dev.nokee.utils.ActionUtils.composite;
import static dev.nokee.utils.ActionUtils.doNothing;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class ActionUtils_CompositeTest {
	@Test
	void canComposeActionsUsingVarargs() {
		val execution = new ArrayList<String>();
		composite(add("first"), add("second")).execute(execution);
		assertThat(execution, contains("first", "second"));
	}

	@Test
	void canComposeActionsUsingList() {
		val execution = new ArrayList<String>();
		composite(of(add("first"), add("second"))).execute(execution);
		assertThat(execution, contains("first", "second"));
	}

	@Test
	void returnsSpecifiedActionWhenComposingOnlyOneAction() {
		ActionUtils.Action<String> action = t -> {};
		assertThat(composite(action), equalTo(action));
		assertThat(composite(of(action)), equalTo(action));
	}

	@Test
	void ignoresActionsThatDoNothing() {
		ActionUtils.Action<String> action = t -> {};
		assertThat(composite(doNothing()), equalTo(doNothing()));
		assertThat(composite(doNothing(), doNothing()), equalTo(doNothing()));
		assertThat(composite(action, doNothing()), equalTo(action));
		assertThat(composite(doNothing(), action), equalTo(action));
	}

	@Test
	void returnsEnhanceAction() {
		Action<String> action = t -> {};
		assertThat(composite(action, action), isA(ActionUtils.Action.class));
	}

	@Test
	void chainingNullActionReturnsTheNullAction() {
		assertThat(doNothing().andThen(doNothing()), equalTo(doNothing()));
	}

	@Test
	void canAndThenActions() {
		val execution = new ArrayList<String>();
		add("first").andThen(add("second")).andThen(add("third")).execute(execution);
		assertThat(execution, contains("first", "second", "third"));
	}

	private static ActionUtils.Action<List<String>> add(String element) {
		return t -> t.add(element);
	}
}
