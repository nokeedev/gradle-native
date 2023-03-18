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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.asLegacyTarget;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.buildArgumentsString;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.buildToolPath;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.passBuildSettingsInEnvironment;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.productName;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.toggle;
import static dev.nokee.internal.testing.GradleRunnerMatchers.outOfDate;
import static java.nio.file.attribute.PosixFilePermissions.fromString;
import static org.hamcrest.MatcherAssert.assertThat;

@EnabledOnOs(OS.MAC)
class UpToDateCheckDetectsChangeToPBXLegacyTargetFunctionalTests extends UpToDateCheckSpec {
	@BeforeEach
	void setup() throws IOException {
		ensureUpToDate(executer);
	}

	@Override
	protected String targetUnderTestName() {
		return "AppLegacy";
	}

	@Test
	void outOfDateWhenBuildToolChanged() throws IOException {
		Files.write(file("my-make"), Arrays.asList("#!/usr/bin/env bash", "make $@"));
		Files.setPosixFilePermissions(file("my-make"), fromString("rwx------"));
		xcodeproj(targetUnderTest(asLegacyTarget(buildToolPath(file("my-make").toString()))));

		assertThat(targetUnderTestExecution(), outOfDate());
	}

	@Test
	void outOfDateWhenBuildArgumentsChanged() {
		xcodeproj(targetUnderTest(asLegacyTarget(buildArgumentsString("build"))));

		assertThat(targetUnderTestExecution(), outOfDate());
	}

	@Test
	void outOfDateWhenProductNameChanged() {
		xcodeproj(targetUnderTest(productName("NewApp")));

		assertThat(targetUnderTestExecution(), outOfDate());
	}

	@Test
	void outOfDateWhenPassBuildSettingsInEnvironmentChanged() {
		xcodeproj(targetUnderTest(asLegacyTarget(passBuildSettingsInEnvironment(toggle()))));

		assertThat(targetUnderTestExecution(), outOfDate());
	}
}
