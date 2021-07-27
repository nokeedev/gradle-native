package dev.nokee.utils;

import com.google.common.testing.EqualsTester;
import lombok.val;
import org.gradle.api.Task;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static dev.nokee.utils.TaskUtils.configureDependsOn;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasToString;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TaskUtils_ConfigureDependsOnTest {
	@Test
	void canConfigureDependsOnSinglePath() {
		val task = Mockito.mock(Task.class);
		configureDependsOn(":foo").execute(task);
		Mockito.verify(task).dependsOn(":foo");
	}

	@Test
	void canConfigureDependsOnMultiplePaths() {
		val task = Mockito.mock(Task.class);
		configureDependsOn(":foo", ":bar").execute(task);
		Mockito.verify(task).dependsOn(":foo", ":bar");
	}

	@Test
	void throwsExceptionWhenPathIsNull() {
		assertThrows(NullPointerException.class, () -> configureDependsOn(null));
		assertThrows(NullPointerException.class, () -> configureDependsOn(":foo", null));
		assertThrows(NullPointerException.class, () -> configureDependsOn(":foo", ":bar", null));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(configureDependsOn(":some:path"), configureDependsOn(":some:path"))
			.addEqualityGroup(configureDependsOn(":foo"))
			.testEquals();
	}

	@Test
	void checkToString() {
		assertThat(configureDependsOn(":some:path"),
			hasToString("TaskUtils.configureDependsOn(:some:path)"));
		assertThat(configureDependsOn(":some:path", ":some:other-path"),
			hasToString("TaskUtils.configureDependsOn(:some:path, :some:other-path)"));
	}
}
