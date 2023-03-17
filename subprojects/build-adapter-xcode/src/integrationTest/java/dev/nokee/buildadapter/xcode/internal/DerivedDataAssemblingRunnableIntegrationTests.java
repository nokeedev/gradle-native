/*
 * Copyright 2023 the original author or authors.
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
package dev.nokee.buildadapter.xcode.internal;

import dev.nokee.buildadapter.xcode.internal.plugins.AssembleDerivedDataDirectoryTask;
import net.nokeedev.testing.junit.jupiter.io.TestDirectory;
import net.nokeedev.testing.junit.jupiter.io.TestDirectoryExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.google.common.collect.ImmutableList.of;
import static dev.nokee.internal.testing.FileSystemMatchers.aFile;
import static dev.nokee.internal.testing.FileSystemMatchers.anExistingFile;
import static dev.nokee.internal.testing.FileSystemMatchers.hasRelativeDescendants;
import static dev.nokee.internal.testing.FileSystemMatchers.ofLines;
import static dev.nokee.internal.testing.FileSystemMatchers.withTextContent;
import static dev.nokee.internal.testing.util.ProjectTestUtils.fileSystemOperations;
import static dev.nokee.internal.testing.util.ProjectTestUtils.objectFactory;
import static java.nio.file.Files.createDirectory;
import static java.nio.file.Files.createFile;
import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(TestDirectoryExtension.class)
class DerivedDataAssemblingRunnableIntegrationTests {
	@TestDirectory Path testDirectory;
	Path derivedDataDirectory;
	AssembleDerivedDataDirectoryTask.DerivedDataAssemblingRunnable.Parameters parameters;
	AssembleDerivedDataDirectoryTask.DerivedDataAssemblingRunnable subject;

	@BeforeEach
	void givenSubject() {
		parameters = objectFactory().newInstance(AssembleDerivedDataDirectoryTask.DerivedDataAssemblingRunnable.Parameters.class);
		subject = new AssembleDerivedDataDirectoryTask.DerivedDataAssemblingRunnable(fileSystemOperations(), parameters);

		parameters.getXcodeDerivedDataPath().set((derivedDataDirectory = testDirectory.resolve("derived-data")).toFile());
	}

	@Nested
	class WithInitialDerivedDataDirectory {
		Path inputDirectory;

		@BeforeEach
		void givenDerivedDataDirectory() throws IOException {
			parameters.getIncomingDerivedDataPaths().from(inputDirectory = createDirectory(testDirectory.resolve("input")));

			createFile(inputDirectory.resolve("a.txt"));
			createDirectory(inputDirectory.resolve("dir"));
			createFile(inputDirectory.resolve("dir/b.txt"));

			subject.run();
		}

		@Nested
		class WhenInputFileChanged {
			@BeforeEach
			void givenChangedInputFile() throws IOException {
				Files.write(inputDirectory.resolve("a.txt"), of("some changes"));

				subject.run();
			}

			@Test // https://github.com/nokeedev/gradle-native/issues/774
			void syncChangesToDerivedDataDirectory() {
				assertThat(derivedDataDirectory, hasRelativeDescendants("a.txt", "dir/b.txt"));
				assertThat(derivedDataDirectory.resolve("a.txt"), aFile(withTextContent(ofLines("some changes"))));
			}
		}

		@Nested
		class WhenDerivedDataFilesRemoved {
			@BeforeEach
			void givenRemovedDerivedDataFile() throws IOException {
				Files.delete(derivedDataDirectory.resolve("dir/b.txt"));
				Files.delete(derivedDataDirectory.resolve("dir"));

				subject.run();
			}

			@Test // https://github.com/nokeedev/gradle-native/issues/774
			void restoresDeletedFiles() {
				assertThat(derivedDataDirectory, hasRelativeDescendants("a.txt", "dir/b.txt"));
			}
		}

		@Nested
		class WhenInputFilesAdded {
			@BeforeEach
			void givenAddedInputFile() throws IOException {
				Files.createFile(inputDirectory.resolve("dir/c.txt"));
				Files.createFile(inputDirectory.resolve("d.txt"));

				subject.run();
			}

			@Test // https://github.com/nokeedev/gradle-native/issues/774
			void syncNewFilesToDerivedDataDirectory() {
				assertThat(derivedDataDirectory, hasRelativeDescendants("a.txt", "dir/b.txt", "dir/c.txt", "d.txt"));
			}
		}
	}

	@Nested
	class WhenDerivedDataContainsAdditionalIntermediateFiles {
		Path anIntermediateFile;

		@BeforeEach
		void givenDerivedDataWithIntermediateFiles() throws IOException {
			createDirectory(derivedDataDirectory);
			anIntermediateFile = createFile(derivedDataDirectory.resolve("intermediate.txt"));

			subject.run();
		}

		@Test // https://github.com/nokeedev/gradle-native/issues/789
		void doesNotDeleteIntermediateFiles() {
			assertThat(anIntermediateFile, anExistingFile());
		}
	}
}
