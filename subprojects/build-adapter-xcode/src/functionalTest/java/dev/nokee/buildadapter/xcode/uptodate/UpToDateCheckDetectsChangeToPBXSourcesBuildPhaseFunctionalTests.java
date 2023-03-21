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
package dev.nokee.buildadapter.xcode.uptodate;

import dev.nokee.xcode.objects.files.PBXReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.add;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.children;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.files;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.first;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.referenceNameOrPath;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.removeIf;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.sourcesBuildPhases;
import static dev.nokee.internal.testing.GradleRunnerMatchers.outOfDate;
import static dev.nokee.internal.testing.GradleRunnerMatchers.upToDate;
import static dev.nokee.xcode.objects.files.PBXFileReference.ofGroup;
import static org.hamcrest.MatcherAssert.assertThat;

@EnabledOnOs(OS.MAC)
class UpToDateCheckDetectsChangeToPBXSourcesBuildPhaseFunctionalTests extends UpToDateCheckSpec {
	@BeforeEach
	void setup() throws IOException {
		ensureUpToDate(executer);
	}

	@Override
	protected String targetUnderTestName() {
		return "App";
	}

	@Nested
	class FilesField {
		@Test
		void outOfDateWhenSourceFileChanged() throws IOException {
			appendChangeToSwiftFile(file("App/ViewController.swift"));

			assertThat(targetUnderTestExecution(), outOfDate());
		}

		@Test
		void outOfDateWhenFileAdded() throws IOException {
			xcodeproj(groupUnderTest(children(add(ofGroup("OtherSource.swift")))));
			xcodeproj(targetUnderTest(sourcesBuildPhases(files(add(buildFileTo("OtherSource.swift"))))));
			Files.write(file("App/OtherSource.swift"), Arrays.asList("// my new file"));

			assertThat(targetUnderTestExecution(), outOfDate());
		}

		@Test
		void outOfDateWhenFileSettingsChanges() {
			xcodeproj(targetUnderTest(sourcesBuildPhases(files(first(changeSettings())))));

			assertThat(targetUnderTestExecution(), outOfDate());
		}

		@Test
		void outOfDateWhenFileRemoved() {
			xcodeproj(targetUnderTest(sourcesBuildPhases(files(removeIf(it -> it.getFileRef().map(PBXReference.class::cast).filter(referenceNameOrPath("FileToRemove.swift")).isPresent())))));

			assertThat(targetUnderTestExecution(), outOfDate());
		}

		@Test
		void outOfDateWhenFileEntryDuplicated() {
			xcodeproj(targetUnderTest(sourcesBuildPhases(files(add(buildFileTo("AppDelegate.swift"))))));

			assertThat(targetUnderTestExecution(), outOfDate());
		}

		@Test
		void outOfDateWhenResolvedFileDuplicated() {
			xcodeproj(alternateFileUnderTest("AppDelegate.swift"));
			xcodeproj(targetUnderTest(sourcesBuildPhases(files(add(buildFileTo("alternate-AppDelegate.swift"))))));

			assertThat(targetUnderTestExecution(), outOfDate());
		}

		@Test
		void outOfDateWhenFilesOrderingChanged() throws IOException {
			xcodeproj(targetUnderTest(sourcesBuildPhases(files(shuffleOrdering()))));

			assertThat(targetUnderTestExecution(), outOfDate());
		}
	}

	@Test
	void ignoresNewUnlinkedSourceFiles() throws IOException {
		xcodeproj(groupUnderTest(children(add(ofGroup("UnusedFile.swift")))));
		Files.write(file("App/UnusedFile.swift"), Arrays.asList("// Some additional line"), StandardOpenOption.WRITE, StandardOpenOption.CREATE);

		assertThat(targetUnderTestExecution(), upToDate());
	}
}
