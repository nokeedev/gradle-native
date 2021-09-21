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
import org.gradle.api.Action;
import org.junit.jupiter.api.Test;
import spock.lang.Subject;

import static dev.nokee.utils.ActionUtils.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@Subject(ActionUtils.class)
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
