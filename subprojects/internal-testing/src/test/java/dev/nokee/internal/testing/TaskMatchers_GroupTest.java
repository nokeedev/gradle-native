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
