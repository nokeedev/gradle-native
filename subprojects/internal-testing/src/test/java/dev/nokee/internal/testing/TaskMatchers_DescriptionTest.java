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
import static dev.nokee.internal.testing.TaskMatchers.description;
import static org.hamcrest.Matchers.*;

class TaskMatchers_DescriptionTest extends AbstractMatcherTest {
	@Override
	protected Matcher<?> createMatcher() {
		return description(equalTo("Some description"));
	}

	private static Task aTaskWithDescription() {
		val result = rootProject().getTasks().create("test");
		result.setDescription("Assembles some outputs");
		return result;
	}

	@Test
	void canCheckMatchingTask() {
		assertMatches(description(containsString("outputs")), aTaskWithDescription(), "matches task description");
	}

	@Test
	void canCheckNonMatchingTask() {
		assertDoesNotMatch(description(equalTo("Something else...")), aTaskWithDescription(),
			"doesn't match task description");
	}

	@Test
	void checkDescription() {
		assertDescription("a task with description of a string starting with \"Assembles\"", description(startsWith("Assembles")));
	}

	@Test
	void checkMismatchDescription() {
		assertMismatchDescription("task description was \"Assembles some outputs\"", description(endsWith("...")), aTaskWithDescription());
	}
}
