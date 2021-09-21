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
package dev.nokee.internal.testing;

import lombok.val;
import org.gradle.api.Task;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.rootProject;
import static dev.nokee.internal.testing.TaskMatchers.group;

class TaskMatchers_GroupTest extends AbstractMatcherTest {
	@Override
	protected Matcher<?> createMatcher() {
		return group("build");
	}

	private static Task aTaskWithBuildGroup() {
		val result = rootProject().getTasks().create("test");
		result.setGroup("build");
		return result;
	}

	@Test
	void canCheckMatchingTask() {
		assertMatches(group("build"), aTaskWithBuildGroup(), "matches task group");
	}

	@Test
	void canCheckNonMatchingTask() {
		assertDoesNotMatch(group("not-build"), aTaskWithBuildGroup(),
			"doesn't match task group");
	}

	@Test
	void checkDescription() {
		assertDescription("a task with group \"build\"", group("build"));
	}

	@Test
	void checkMismatchDescription() {
		assertMismatchDescription("task group was \"build\"", group("not-build"), aTaskWithBuildGroup());
	}
}
