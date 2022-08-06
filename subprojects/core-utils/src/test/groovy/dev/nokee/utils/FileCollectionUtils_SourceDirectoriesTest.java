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

import lombok.val;
import net.nokeedev.testing.junit.jupiter.io.TestDirectory;
import net.nokeedev.testing.junit.jupiter.io.TestDirectoryExtension;
import org.gradle.api.file.FileCollection;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static dev.nokee.internal.testing.FileSystemMatchers.aFile;
import static dev.nokee.internal.testing.FileSystemMatchers.withAbsolutePath;
import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static dev.nokee.internal.testing.ProjectMatchers.buildDependencies;
import static dev.nokee.internal.testing.util.ProjectTestUtils.objectFactory;
import static dev.nokee.internal.testing.util.ProjectTestUtils.rootProject;
import static dev.nokee.utils.FileCollectionUtils.sourceDirectories;
import static java.nio.file.Files.createDirectories;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.endsWith;

@ExtendWith(TestDirectoryExtension.class)
class FileCollectionUtils_SourceDirectoriesTest {
	@TestDirectory Path testDirectory;

	abstract class Tester {
		abstract FileCollection createFileCollection(Object... paths);

		@Test
		void assumesMissingFilesAsSourceDirectories() {
			val testPath = testDirectory.resolve("missing-path");
			val subject = sourceDirectories(createFileCollection(testPath));
			assertThat(subject, providerOf(contains(aFile(withAbsolutePath(endsWith("/missing-path"))))));
		}

		@Test
		void usesParentDirectoryOfRegularFiles() throws IOException {
			val testPath = createFile(createParentDirectories(testDirectory.resolve("some-parent-dir/some-file.txt")));
			val subject = sourceDirectories(createFileCollection(testPath));
			assertThat(subject, providerOf(contains(aFile(withAbsolutePath(endsWith("/some-parent-dir"))))));
		}

		@Test
		void usesDirectoryAsSourceDirectories() throws IOException {
			val testPath = createDirectories(testDirectory.resolve("some-parent-dir/some-dir"));
			val subject = sourceDirectories(createFileCollection(testPath));
			assertThat(subject, providerOf(contains(aFile(withAbsolutePath(endsWith("/some-parent-dir/some-dir"))))));
		}

		@Test
		void usesBaseDirectoryOfFileTreeAsSourceDirectories() throws IOException {
			val testPath = createDirectories(testDirectory.resolve("some-base-dir"));
			createFile(testPath.resolve("foo.txt"));
			createFile(testPath.resolve("bar.txt"));
			createFile(testPath.resolve("far.ignored"));
			val subject = sourceDirectories(createFileCollection(objectFactory().fileTree().setDir(testPath).include("**/*.txt")));
			assertThat(subject, providerOf(contains(aFile(withAbsolutePath(endsWith("/some-base-dir"))))));
		}

		@Test
		void forwardsImplicitTaskDependencies() throws IOException {
			val generatorTask = rootProject().getTasks().register("generator");
			val testPath = createDirectories(testDirectory.resolve("some-dir"));
			val subject = sourceDirectories(createFileCollection(generatorTask.map(it -> testPath)));

			assertThat(objectFactory().fileCollection().from(subject), buildDependencies(contains(named("generator"))));
		}
	}

	@Nested
	class NestedFileCollectionTest extends Tester {
		@Override
		FileCollection createFileCollection(Object... paths) {
			return objectFactory().fileCollection().from(objectFactory().fileCollection().from(paths));
		}
	}

	@Nested
	class DefaultFileCollectionTest extends Tester {
		@Override
		FileCollection createFileCollection(Object... paths) {
			return objectFactory().fileCollection().from(paths);
		}
	}

	private static Path createParentDirectories(Path self) throws IOException {
		Files.createDirectories(self.getParent());
		return self;
	}

	private static Path createFile(Path self) throws IOException {
		self.toFile().createNewFile();
		return self;
	}
}
