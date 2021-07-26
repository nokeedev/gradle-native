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
