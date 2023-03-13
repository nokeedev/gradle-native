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

import com.google.common.base.Predicates;
import dev.nokee.xcode.objects.buildphase.PBXShellScriptBuildPhase;
import lombok.val;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

import static com.google.common.base.Predicates.instanceOf;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.add;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.asGroup;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.asShellScript;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.buildPhases;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.childNamed;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.children;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.inputPaths;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.mainGroup;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.matching;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.mutateProject;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.removeFirst;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.scriptPhaseName;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.shellPath;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.shellScript;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.targetNamed;
import static dev.nokee.internal.testing.GradleRunnerMatchers.outOfDate;
import static dev.nokee.internal.testing.GradleRunnerMatchers.upToDate;
import static dev.nokee.xcode.objects.files.PBXFileReference.ofGroup;
import static java.nio.file.Files.delete;
import static org.hamcrest.MatcherAssert.assertThat;

@EnabledOnOs(OS.MAC)
class UpToDateCheckDetectsChangeToPBXShellScriptBuildPhaseFunctionalTests extends UpToDateCheckSpec {
	void setup(Path location) {
		mutateProject(targetNamed("App", buildPhases(add(PBXShellScriptBuildPhase.builder().name("Lint main component").shellScript("echo \"dummy-result\" > \"$DERIVED_FILE_DIR/App-result.txt\"").inputPath("$(SRCROOT)/App/AppDelegate.swift").inputPath("$(SRCROOT)/App/ViewController.swift").outputPath("$(DERIVED_FILE_DIR)/App-result.txt").build())))).accept(location);
	}

	@Nested
	class InputPathsField {
		@Test
		void outOfDateWhenFileContentChange() throws IOException {
			appendChangeToSwiftFile(testDirectory.resolve("App/AppDelegate.swift"));

			assertThat(executer.build().task(":UpToDateCheck:AppDebug"), outOfDate());
		}

		@Test
		void outOfDateWhenNewFileEntry() throws IOException {
			mutateProject(mainGroup(childNamed("App", asGroup(children(add(ofGroup("NewFile.swift"))))))).accept(testDirectory.resolve("UpToDateCheck.xcodeproj"));
			mutateProject(targetNamed("App", buildPhases(matching(instanceOf(PBXShellScriptBuildPhase.class), asShellScript(inputPaths(add("$(SRCROOT)/App/NewFile.swift"))))))).accept(testDirectory.resolve("UpToDateCheck.xcodeproj"));
			Files.write(testDirectory.resolve("App/NewFile.swift"), Arrays.asList("// Some additional line"), StandardOpenOption.WRITE, StandardOpenOption.CREATE);

			assertThat(executer.build().task(":UpToDateCheck:AppDebug"), outOfDate());
		}

		@Test
		@Disabled
		void ignoresDuplicatedPaths() {
			mutateProject(targetNamed("App", buildPhases(matching(instanceOf(PBXShellScriptBuildPhase.class), asShellScript(inputPaths(add("$(SRCROOT)/App/AppDelegate.swift"))))))).accept(testDirectory.resolve("UpToDateCheck.xcodeproj"));

			assertThat(executer.build().task(":UpToDateCheck:AppDebug"), upToDate());
		}

		@Test
		@Disabled
		void ignoresPathsThatResolvesToDuplicatedFiles() {
			mutateProject(targetNamed("App", buildPhases(matching(instanceOf(PBXShellScriptBuildPhase.class), asShellScript(inputPaths(add("$(SOURCE_ROOT)/App/AppDelegate.swift"))))))).accept(testDirectory.resolve("UpToDateCheck.xcodeproj"));

			assertThat(executer.build().task(":UpToDateCheck:AppDebug"), upToDate());
		}

		@Test
		void outOfDateWhenFilesRemoved() {
			mutateProject(targetNamed("App", buildPhases(matching(instanceOf(PBXShellScriptBuildPhase.class), asShellScript(inputPaths(removeFirst())))))).accept(testDirectory.resolve("UpToDateCheck.xcodeproj"));

			assertThat(executer.build().task(":UpToDateCheck:AppDebug"), outOfDate());
		}
	}

	@Test
	void ignoresNewUnlinkedFile() throws IOException {
		mutateProject(mainGroup(childNamed("App", asGroup(children(add(ofGroup("UnusedFile.swift"))))))).accept(testDirectory.resolve("UpToDateCheck.xcodeproj"));
		Files.write(testDirectory.resolve("App/UnusedFile.swift"), Arrays.asList("// Some additional line"), StandardOpenOption.WRITE, StandardOpenOption.CREATE);

		assertThat(executer.build().task(":UpToDateCheck:AppDebug"), upToDate());
	}

	@Test
	void ignoresChangesToBuildPhaseName() {
		mutateProject(targetNamed("App", buildPhases(matching(instanceOf(PBXShellScriptBuildPhase.class), asShellScript(scriptPhaseName("Lint 'App' component")))))).accept(testDirectory.resolve("UpToDateCheck.xcodeproj"));

		assertThat(executer.build().task(":UpToDateCheck:AppDebug"), upToDate());
	}

	@Disabled("outputs are not yet tracked")
	@Test
	void outOfDateWhenOutputFileRemoved() throws IOException {
		delete(buildDirectory().resolve("tmp/AppDebug/derivedData/Build/Intermediates.noindex/UpToDateCheck.build/Debug/App.build/DerivedSources/App-result.txt"));

		assertThat(executer.build().task(":UpToDateCheck:AppDebug"), outOfDate());
	}

	@Test
	void outOfDateWhenShellPathChange() {
		mutateProject(targetNamed("App", buildPhases(matching(instanceOf(PBXShellScriptBuildPhase.class), asShellScript(shellPath("/bin/bash")))))).accept(testDirectory.resolve("UpToDateCheck.xcodeproj"));

		assertThat(executer.build().task(":UpToDateCheck:AppDebug"), outOfDate());
	}

	@Test
	void outOfDateWhenShellScriptChange() {
		mutateProject(targetNamed("App", buildPhases(matching(instanceOf(PBXShellScriptBuildPhase.class), asShellScript(shellScript("echo \"less-dummy-result\" > \"$DERIVED_FILE_DIR/App-result.txt\"")))))).accept(testDirectory.resolve("UpToDateCheck.xcodeproj"));

		assertThat(executer.build().task(":UpToDateCheck:AppDebug"), outOfDate());
	}
}
