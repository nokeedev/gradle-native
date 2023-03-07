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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.add;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.asGroup;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.buildPhases;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.childNamed;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.children;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.mainGroup;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.mutateProject;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.targetNamed;
import static dev.nokee.internal.testing.GradleRunnerMatchers.outOfDate;
import static dev.nokee.internal.testing.GradleRunnerMatchers.upToDate;
import static dev.nokee.xcode.objects.files.PBXFileReference.ofGroup;
import static java.nio.file.Files.delete;
import static org.hamcrest.MatcherAssert.assertThat;

class UpToDateCheckDetectsChangeToPBXShellScriptBuildPhaseFunctionalTests extends UpToDateCheckSpec {
	void setup(Path location) {
		mutateProject(targetNamed("App", buildPhases(add(PBXShellScriptBuildPhase.builder().shellScript("echo \"dummy-result\" > \"$DERIVED_FILE_DIR/App-result.txt\"").inputPath("$(SRCROOT)/App/AppDelegate.swift").inputPath("$(SRCROOT)/App/ViewController.swift").outputPath("$(DERIVED_FILE_DIR)/App-result.txt").build())))).accept(location);
	}

	@Test
	void outOfDateWhenInputFileChange() throws IOException {
		appendChangeToSwiftFile(testDirectory.resolve("App/AppDelegate.swift"));

		assertThat(executer.build().task(":UpToDateCheck:AppDebug"), outOfDate());
	}

	@Test
	void ignoresNewUnlinkedFile() throws IOException {
		mutateProject(mainGroup(childNamed("App", asGroup(children(add(ofGroup("UnusedFile.swift"))))))).accept(testDirectory.resolve("UpToDateCheck.xcodeproj"));
		Files.write(testDirectory.resolve("App/UnusedFile.swift"), Arrays.asList("// Some additional line"), StandardOpenOption.WRITE, StandardOpenOption.CREATE);

		assertThat(executer.build().task(":UpToDateCheck:AppDebug"), upToDate());
	}

	@Disabled("outputs are not yet tracked")
	@Test
	void outOfDateWhenOutputFileRemoved() throws IOException {
		delete(buildDirectory().resolve("tmp/AppDebug/derivedData/Build/Intermediates.noindex/UpToDateCheck.build/Debug/App.build/DerivedSources/App-result.txt"));

		assertThat(executer.build().task(":UpToDateCheck:AppDebug"), outOfDate());
	}
}
