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

import dev.gradleplugins.runnerkit.GradleRunner;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.asLegacyTarget;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.buildArgumentsString;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.buildToolPath;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.mutateProject;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.targetNamed;
import static dev.nokee.internal.testing.GradleRunnerMatchers.outOfDate;
import static java.nio.file.attribute.PosixFilePermissions.fromString;
import static org.hamcrest.MatcherAssert.assertThat;

class UpToDateCheckDetectsChangeToPBXLegacyTargetFunctionalTests extends UpToDateCheckSpec {
	@Override
	void setup(Path location) throws IOException {
		Files.write(location.getParent().resolve("makefile"), Arrays.asList("null:", "\t@:"));
	}

	@Override
	GradleRunner configure(GradleRunner runner) {
		return runner.withTasks("LegacyDebug");
	}

	@Test
	void outOfDateWhenBuildToolChange() throws IOException {
		Files.write(testDirectory.resolve("my-make"), Arrays.asList("#!/usr/bin/env bash", "make $@"));
		Files.setPosixFilePermissions(testDirectory.resolve("my-make"), fromString("rwx------"));
		mutateProject(targetNamed("Legacy", asLegacyTarget(buildToolPath(testDirectory.resolve("my-make").toString())))).accept(testDirectory.resolve("UpToDateCheck.xcodeproj"));

		assertThat(executer.build().task(":UpToDateCheck:LegacyDebug"), outOfDate());
	}

	@Test
	void outOfDateWhenBuildArgumentsChange() {
		mutateProject(targetNamed("Legacy", asLegacyTarget(buildArgumentsString("build")))).accept(testDirectory.resolve("UpToDateCheck.xcodeproj"));

		assertThat(executer.build().task(":UpToDateCheck:LegacyDebug"), outOfDate());
	}
}
