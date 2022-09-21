/*
 * Copyright 2022 the original author or authors.
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
package dev.nokee.buildadapter.xcode;

import dev.nokee.buildadapter.xcode.internal.DefaultGradleProjectPathService;
import dev.nokee.buildadapter.xcode.internal.GradleProjectPathService;
import dev.nokee.samples.xcode.EmptyProject;
import dev.nokee.xcode.XCProjectReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.gradle.util.Path.path;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefaultGradleProjectPathServiceTests {
	@TempDir Path testDirectory;
	Path workspaceDirectory;
	GradleProjectPathService subject;

	@BeforeEach
	void createSubject() {
		subject = new DefaultGradleProjectPathService(workspaceDirectory = testDirectory.resolve("work"));
	}

	@Test
	void returnsProjectPathOfXcodeProject() {
		new EmptyProject().writeToProject(workspaceDirectory);
		XCProjectReference reference = XCProjectReference.of(workspaceDirectory.resolve("Empty.xcodeproj"));
		assertThat(subject.toProjectPath(reference), equalTo(path(":Empty")));
	}

	@Test
	void returnsProjectPathOfXcodeProjectIncludingRelativeParentDirectory() {
		new EmptyProject().writeToProject(workspaceDirectory.resolve("a/b"));
		XCProjectReference reference = XCProjectReference.of(workspaceDirectory.resolve("a/b/Empty.xcodeproj"));
		assertThat(subject.toProjectPath(reference), equalTo(path(":a:b:Empty")));
	}

	@Test
	void throwsExceptionWhenXcodeProjectIsOutsideWorkspace() {
		new EmptyProject().writeToProject(testDirectory);
		XCProjectReference reference = XCProjectReference.of(testDirectory.resolve("Empty.xcodeproj"));
		assertThrows(IllegalArgumentException.class, () -> subject.toProjectPath(reference));
	}
}
