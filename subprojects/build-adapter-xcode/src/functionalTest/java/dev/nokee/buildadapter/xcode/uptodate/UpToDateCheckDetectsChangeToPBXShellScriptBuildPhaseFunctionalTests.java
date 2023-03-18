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

import dev.nokee.xcode.objects.buildphase.PBXShellScriptBuildPhase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.add;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.buildPhases;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.children;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.inputPaths;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.removeFirst;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.scriptPhaseName;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.shellPath;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.shellScript;
import static dev.nokee.internal.testing.GradleRunnerMatchers.outOfDate;
import static dev.nokee.internal.testing.GradleRunnerMatchers.upToDate;
import static dev.nokee.xcode.objects.files.PBXFileReference.ofGroup;
import static java.nio.file.Files.delete;
import static org.hamcrest.MatcherAssert.assertThat;

@EnabledOnOs(OS.MAC)
class UpToDateCheckDetectsChangeToPBXShellScriptBuildPhaseFunctionalTests extends UpToDateCheckSpec {
	@BeforeEach
	void setup() throws IOException {
		xcodeproj(targetUnderTest(buildPhases(add(PBXShellScriptBuildPhase.builder()
			.name("Lint main component")
			.shellScript("echo \"dummy-result\" > \"$DERIVED_FILE_DIR/App-result.txt\"")
			.inputPath("$(SRCROOT)/App/AppDelegate.swift")
			.inputPath("$(SRCROOT)/App/ViewController.swift")
			.outputPath("$(DERIVED_FILE_DIR)/App-result.txt")
			.build()))));

		ensureUpToDate(executer);
	}

	@Override
	protected String targetUnderTestName() {
		return "App";
	}

	@Nested
	class InputPathsField {
		@Test
		void outOfDateWhenInputPathContentChanged() throws IOException {
			appendChangeToSwiftFile(file("App/AppDelegate.swift"));

			assertThat(targetUnderTestExecution(), outOfDate());
		}

		@Test
		void outOfDateWhenInputPathAdded() {
			xcodeproj(groupUnderTest(children(add(ofGroup("NewFile.swift")))));
			xcodeproj(targetUnderTest(shellScriptBuildPhases(inputPaths(add("$(SRCROOT)/App/NewFile.swift")))));
			xcodeproj(run(() -> Files.write(file("App/NewFile.swift"), Arrays.asList("// Some additional line"), StandardOpenOption.WRITE, StandardOpenOption.CREATE)));

			assertThat(targetUnderTestExecution(), outOfDate());
		}

		@Test
		void ignoresDuplicatedInputPaths() {
			xcodeproj(targetUnderTest(shellScriptBuildPhases(inputPaths(add("$(SRCROOT)/App/AppDelegate.swift")))));

			assertThat(targetUnderTestExecution(), upToDate());
		}

		@Test
		void ignoresDuplicatedResolvedInputPaths() {
			xcodeproj(targetUnderTest(shellScriptBuildPhases(inputPaths(add("$(SOURCE_ROOT)/App/AppDelegate.swift")))));

			assertThat(targetUnderTestExecution(), upToDate());
		}

		@Test
		void ignoresInputPathsOrderingChanges() {
			xcodeproj(targetUnderTest(shellScriptBuildPhases(inputPaths(shuffleOrdering()))));

			assertThat(targetUnderTestExecution(), upToDate());
		}

		@Test
		void outOfDateWhenInputPathRemoved() {
			xcodeproj(targetUnderTest(shellScriptBuildPhases(inputPaths(removeFirst()))));

			assertThat(targetUnderTestExecution(), outOfDate());
		}
	}

	@Test
	void ignoresNewUnlinkedFile() throws IOException {
		xcodeproj(groupUnderTest(children(add(ofGroup("UnusedFile.swift")))));
		Files.write(file("App/UnusedFile.swift"), Arrays.asList("// Some additional line"), StandardOpenOption.WRITE, StandardOpenOption.CREATE);

		assertThat(targetUnderTestExecution(), upToDate());
	}

	@Test
	void ignoresChangesToBuildPhaseName() {
		xcodeproj(targetUnderTest(shellScriptBuildPhases(scriptPhaseName("Lint 'App' component"))));

		assertThat(targetUnderTestExecution(), upToDate());
	}

	@Disabled("outputs are not yet tracked")
	@Test
	void outOfDateWhenOutputFileRemoved() throws IOException {
		delete(buildDirectory().resolve("tmp/AppDebug/derivedData/Build/Intermediates.noindex/UpToDateCheck.build/Debug/App.build/DerivedSources/App-result.txt"));

		assertThat(targetUnderTestExecution(), outOfDate());
	}

	@Test
	void outOfDateWhenShellPathChanged() {
		xcodeproj(targetUnderTest(shellScriptBuildPhases(shellPath("/bin/bash"))));

		assertThat(targetUnderTestExecution(), outOfDate());
	}

	@Test
	void outOfDateWhenShellScriptChanged() {
		xcodeproj(targetUnderTest(shellScriptBuildPhases(shellScript("echo \"less-dummy-result\" > \"$DERIVED_FILE_DIR/App-result.txt\""))));

		assertThat(targetUnderTestExecution(), outOfDate());
	}
}
