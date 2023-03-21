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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.io.IOException;

import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.add;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.children;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.clear;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.files;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.first;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.resourcesBuildPhases;
import static dev.nokee.internal.testing.GradleRunnerMatchers.outOfDate;
import static dev.nokee.internal.testing.GradleRunnerMatchers.upToDate;
import static dev.nokee.xcode.objects.files.PBXFileReference.ofGroup;
import static org.hamcrest.MatcherAssert.assertThat;

@EnabledOnOs(OS.MAC)
class UpToDateCheckDetectsChangeToPBXResourcesBuildPhaseFunctionalTests extends UpToDateCheckSpec {
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
		void outOfDateWhenXCAssetsChanged() throws IOException {
			writeColorSet(file("App/Assets.xcassets/NewColor.colorset"));

			assertThat(targetUnderTestExecution(), outOfDate());
		}

		@Test
		void outOfDateWhenFileAdded() {
			xcodeproj(groupUnderTest(children(add(ofGroup("OtherAssets.xcassets")))));
			xcodeproj(targetUnderTest(resourcesBuildPhases(files(add(buildFileTo("OtherAssets.xcassets"))))));
			xcodeproj(run(() -> writeColorSet(file("App/OtherAssets.xcassets/NewColor.colorset"))));

			assertThat(targetUnderTestExecution(), outOfDate());
		}

		@Test
		void outOfDateWhenFileSettingsChanges() {
			xcodeproj(targetUnderTest(resourcesBuildPhases(files(first(changeSettings())))));

			assertThat(targetUnderTestExecution(), outOfDate());
		}

		@Test
		void outOfDateWhenFileRemoved() {
			xcodeproj(targetUnderTest(resourcesBuildPhases(files(clear()))));

			assertThat(targetUnderTestExecution(), outOfDate());
		}

		@Test
		void outOfDateWhenFileEntryDuplicated() {
			xcodeproj(targetUnderTest(resourcesBuildPhases(files(add(buildFileTo("Assets.xcassets"))))));

			assertThat(targetUnderTestExecution(), outOfDate());
		}

		@Test
		void outOfDateWhenResolvedFileDuplicated() {
			xcodeproj(alternateFileUnderTest("Assets.xcassets"));
			xcodeproj(targetUnderTest(resourcesBuildPhases(files(add(buildFileTo("alternate-Assets.xcassets"))))));

			assertThat(targetUnderTestExecution(), outOfDate());
		}

		@Test
		void outOfDateWhenFileOrderingChanged() throws IOException {
			writeColorSet(file("App/OtherAssets.xcassets/NewColor.colorset"));
			xcodeproj(groupUnderTest(children(add(ofGroup("OtherAssets.xcassets")))));
			xcodeproj(targetUnderTest(resourcesBuildPhases(files(add(buildFileTo("OtherAssets.xcassets"))))));

			ensureUpToDate(executer);

			xcodeproj(targetUnderTest(resourcesBuildPhases(files(shuffleOrdering()))));

			assertThat(targetUnderTestExecution(), outOfDate());
		}
	}

	@Test
	void ignoresNewUnlinkedAssets() throws IOException {
		writeColorSet(file("App/OtherAssets.xcassets/NewColor.colorset"));
		xcodeproj(groupUnderTest(children(add(ofGroup("OtherAssets.xcassets")))));

		assertThat(targetUnderTestExecution(), upToDate());
	}

	// TODO: Modifying storyboard should make this out-of-date but it doesn't work because how we calculate paths for PBXVariantGroup is broken
}
