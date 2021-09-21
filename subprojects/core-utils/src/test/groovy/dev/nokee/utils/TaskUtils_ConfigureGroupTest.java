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
import org.gradle.api.Task;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static dev.nokee.utils.TaskUtils.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.isA;

class TaskUtils_ConfigureGroupTest {
	@Test
	void canConfigureGroup() {
		val task = Mockito.mock(Task.class);
		configureGroup("foo").execute(task);
		Mockito.verify(task).setGroup("foo");
	}

	@Test
	void canConfigureBuildGroup() {
		val task = Mockito.mock(Task.class);
		configureBuildGroup().execute(task);
		Mockito.verify(task).setGroup("build");
	}

	@Test
	void canConfigureVerificationGroup() {
		val task = Mockito.mock(Task.class);
		configureVerificationGroup().execute(task);
		Mockito.verify(task).setGroup("verification");
	}

	@Test
	void returnsEnhanceAction() {
		assertThat(configureGroup("foo"), isA(ActionUtils.Action.class));
		assertThat(configureBuildGroup(), isA(ActionUtils.Action.class));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(configureGroup("foo"), configureGroup("foo"))
			.addEqualityGroup(configureGroup("build"), configureBuildGroup())
			.addEqualityGroup(configureGroup("verification"))
			.testEquals();
	}

	@Test
	void checkToString() {
		assertThat(configureGroup("foo"), hasToString("TaskUtils.configureGroup(foo)"));
		assertThat(configureBuildGroup(), hasToString("TaskUtils.configureGroup(build)"));
		assertThat(configureGroup("verification"), hasToString("TaskUtils.configureGroup(verification)"));
		assertThat(configureVerificationGroup(), hasToString("TaskUtils.configureGroup(verification)"));
	}
}
