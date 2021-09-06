package dev.nokee.utils;

import lombok.val;
import org.gradle.api.Task;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.rootProject;
import static dev.nokee.internal.testing.FileSystemMatchers.aFile;
import static dev.nokee.internal.testing.FileSystemMatchers.withAbsolutePath;
import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static dev.nokee.utils.TaskUtils.temporaryDirectoryPath;
import static dev.nokee.utils.TaskUtils.temporaryTaskDirectory;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;

class TaskUtils_TemporaryTaskDirectoryTest {
	@Test
	void canGetTaskTemporaryDirectoryPath() {
		val task = Mockito.mock(Task.class);
		Mockito.when(task.getName()).thenReturn("foo");
		assertThat(temporaryDirectoryPath(task), equalTo("tmp/foo"));
	}

	@Test
	void canGetTemporaryTaskDirectory() {
		val task = rootProject().getTasks().create("hgwu");
		assertThat(temporaryTaskDirectory(task), providerOf(aFile(withAbsolutePath(endsWith("/build/tmp/hgwu")))));
	}
}
