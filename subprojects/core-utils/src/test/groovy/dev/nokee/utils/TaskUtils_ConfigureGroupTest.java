package dev.nokee.utils;

import com.google.common.testing.EqualsTester;
import lombok.val;
import org.gradle.api.Task;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static dev.nokee.utils.TaskUtils.configureGroup;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasToString;

class TaskUtils_ConfigureGroupTest {
	@Test
	void canConfigureGroup() {
		val task = Mockito.mock(Task.class);
		configureGroup("build").execute(task);
		Mockito.verify(task).setGroup("build");
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(configureGroup("build"), configureGroup("build"))
			.addEqualityGroup(configureGroup("verification"))
			.testEquals();
	}

	@Test
	void checkToString() {
		assertThat(configureGroup("build"), hasToString("TaskUtils.configureGroup(build)"));
		assertThat(configureGroup("verification"), hasToString("TaskUtils.configureGroup(verification)"));
	}
}
