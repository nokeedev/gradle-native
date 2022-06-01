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
package dev.nokee.utils;

import net.nokeedev.testing.junit.jupiter.io.TestDirectory;
import net.nokeedev.testing.junit.jupiter.io.TestDirectoryExtension;
import org.gradle.api.file.Directory;
import org.gradle.api.file.RegularFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.nio.file.Path;

import static dev.nokee.internal.testing.util.ProjectTestUtils.objectFactory;
import static dev.nokee.utils.FileSystemLocationUtils.asPath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(TestDirectoryExtension.class)
class FileSystemLocationUtils_AsPathTest {
	@TestDirectory Path testDirectory;

	@Test
	void canCastDirectoryToJavaPath() {
		assertThat(asPath(newDirectory("some/dir")), equalTo(testDirectory.resolve("some/dir")));
	}

	@Test
	void canCastRegularFileToJavaPath() {
		assertThat(asPath(newRegularFile("some/foo.txt")), equalTo(testDirectory.resolve("some/foo.txt")));
	}

	@Test
	void throwsExceptionWhenFileSystemLocationIsNull() {
		assertThrows(NullPointerException.class, () -> asPath(null));
	}

	private Directory newDirectory(String path) {
		return objectFactory().directoryProperty().fileValue(testDirectory.resolve(path).toFile()).get();
	}

	private RegularFile newRegularFile(String path) {
		return objectFactory().fileProperty().fileValue(testDirectory.resolve(path).toFile()).get();
	}
}
