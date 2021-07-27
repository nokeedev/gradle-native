package dev.nokee.utils;

import com.google.common.testing.EqualsTester;
import lombok.val;
import org.gradle.api.Task;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static dev.nokee.utils.TaskUtils.configureBuildGroup;
import static dev.nokee.utils.TaskUtils.configureGroup;
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
	}
}
