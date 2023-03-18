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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.io.IOException;

import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.add;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.buildFileToProduct;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.clear;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.files;
import static dev.nokee.internal.testing.GradleRunnerMatchers.outOfDate;
import static java.nio.file.Files.delete;
import static org.hamcrest.MatcherAssert.assertThat;

@EnabledOnOs(OS.MAC)
class UpToDateCheckDetectsChangeToPBXFrameworksBuildPhaseFunctionalTests extends UpToDateCheckSpec {
	@BeforeEach
	void setup() throws IOException {
		xcodeproj(targetUnderTest(frameworksBuildPhases(files(add(buildFileToProduct("Foo.framework"))))));

		ensureUpToDate(executer);
	}

	@Override
	protected String targetUnderTestName() {
		return "App";
	}

	@Nested
	class FilesField {
		@Test
		void outOfDateWhenConsumedFrameworkRebuilt() throws IOException {
			appendChangeToSwiftFile(file("Foo/Foo.swift"));

			assertThat(targetUnderTestExecution(), outOfDate());
		}

		@Test
		void outOfDateWhenFileAdded() {
			xcodeproj(targetUnderTest(frameworksBuildPhases(files(add(buildFileToProduct("Bar.framework"))))));

			assertThat(targetUnderTestExecution(), outOfDate());
		}

		@Test
		void outOfDateWhenFileRemoved() {
			xcodeproj(targetUnderTest(frameworksBuildPhases(files(clear()))));

			assertThat(targetUnderTestExecution(), outOfDate());
		}

		@Test
		void outOfDateWhenFileEntryDuplicated() {
			xcodeproj(targetUnderTest(frameworksBuildPhases(files(add(buildFileToProduct("Foo.framework"))))));

			assertThat(targetUnderTestExecution(), outOfDate());
		}

		@Test
		void outOfDateWhenResolvedFileDuplicated() {
			xcodeproj(alternateBuiltProduct("Foo.framework"));
			xcodeproj(targetUnderTest(frameworksBuildPhases(files(add(buildFileToProduct("alternate-Foo.framework"))))));

			assertThat(targetUnderTestExecution(), outOfDate());
		}

		@Test
		void outOfDateWhenFileOrderingChanged() {
			xcodeproj(targetUnderTest(frameworksBuildPhases(files(add(buildFileToProduct("Bar.framework"))))));

			ensureUpToDate(executer.withArgument("-i"));

			xcodeproj(targetUnderTest(frameworksBuildPhases(files(shuffleOrdering()))));

			assertThat(targetUnderTestExecution(), outOfDate());
		}
	}

	@Disabled("outputs are not yet tracked")
	@Test
	void outOfDateWhenConsumedFrameworkChange() throws IOException {
		delete(appDebugProductsDirectory().resolve("Foo.framework/Foo"));

		assertThat(targetUnderTestExecution(), outOfDate());
	}
}
