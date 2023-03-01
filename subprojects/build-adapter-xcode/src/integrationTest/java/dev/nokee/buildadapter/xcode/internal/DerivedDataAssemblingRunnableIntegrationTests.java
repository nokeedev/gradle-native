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

import dev.nokee.buildadapter.xcode.internal.plugins.XcodeTargetExecTask;
import dev.nokee.internal.testing.FileSystemMatchers;
import dev.nokee.internal.testing.testdoubles.MockitoBuilder;
import dev.nokee.internal.testing.util.ProjectTestUtils;
import net.nokeedev.testing.junit.jupiter.io.TestDirectory;
import net.nokeedev.testing.junit.jupiter.io.TestDirectoryExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static dev.nokee.internal.testing.FileSystemMatchers.anExistingFile;
import static dev.nokee.internal.testing.util.ProjectTestUtils.fileSystemOperations;
import static dev.nokee.internal.testing.util.ProjectTestUtils.objectFactory;
import static java.nio.file.Files.createDirectory;
import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(TestDirectoryExtension.class)
class DerivedDataAssemblingRunnableIntegrationTests {
	@TestDirectory Path testDirectory;
	Path outputDirectory;
	Path derivedDataDirectory;
	XcodeTargetExecTask.DerivedDataAssemblingRunnable.Parameters parameters;
	XcodeTargetExecTask.DerivedDataAssemblingRunnable subject;
	Runnable delegate = MockitoBuilder.newMock(Runnable.class).instance();

	@BeforeEach
	void givenSubject() {
		parameters = objectFactory().newInstance(XcodeTargetExecTask.DerivedDataAssemblingRunnable.Parameters.class);
		subject = new XcodeTargetExecTask.DerivedDataAssemblingRunnable(fileSystemOperations(), parameters, delegate);

		parameters.getOutgoingDerivedDataPath().set((outputDirectory = testDirectory.resolve("output")).toFile());
		parameters.getXcodeDerivedDataPath().set((derivedDataDirectory = testDirectory.resolve("derived-data")).toFile());
	}

	@Nested
	class WhenDerivedDataContainsAdditionalIntermediateFiles {
		Path anIntermediateFile;

		@BeforeEach
		void givenDerivedDataWithIntermediateFiles() throws IOException {
			createDirectory(derivedDataDirectory);
			anIntermediateFile = Files.createFile(derivedDataDirectory.resolve("intermediate.txt"));

			subject.run();
		}

		@Test // https://github.com/nokeedev/gradle-native/issues/789
		void doesNotDeleteIntermediateFiles() {
			assertThat(anIntermediateFile, anExistingFile());
		}
	}
}
