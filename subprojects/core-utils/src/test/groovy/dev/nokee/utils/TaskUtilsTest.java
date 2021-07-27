package dev.nokee.utils;

import lombok.val;
import org.gradle.api.Task;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static dev.nokee.utils.TaskUtils.temporaryDirectoryPath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class TaskUtilsTest {
	@Test
	void canGetTaskTemporaryDirectoryPath() {
		val task = Mockito.mock(Task.class);
		Mockito.when(task.getName()).thenReturn("foo");
		assertThat(temporaryDirectoryPath(task), equalTo("tmp/foo"));
	}
}
