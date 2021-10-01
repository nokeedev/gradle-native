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

import lombok.val;
import org.gradle.api.Task;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static dev.nokee.internal.testing.util.ProjectTestUtils.rootProject;
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
