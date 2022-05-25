/*
 * Copyright 2022 the original author or authors.
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
package dev.nokee.init;

import dev.gradleplugins.runnerkit.BuildResult;
import dev.gradleplugins.runnerkit.GradleRunner;
import dev.nokee.internal.testing.junit.jupiter.ContextualGradleRunnerParameterResolver;
import net.nokeedev.testing.junit.jupiter.io.TestDirectory;
import net.nokeedev.testing.junit.jupiter.io.TestDirectoryExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static dev.gradleplugins.buildscript.blocks.PluginsBlock.plugins;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

@ExtendWith({TestDirectoryExtension.class, ContextualGradleRunnerParameterResolver.class})
class NokeeVersionManagementServiceNoVersionFunctionalTest {
	@TestDirectory Path testDirectory;
	GradleRunner executer;

	@BeforeEach
	void setup(GradleRunner runner) throws IOException {
		executer = runner;
		plugins(it -> it.id("dev.nokee.nokee-version-management")).writeTo(testDirectory.resolve("settings.gradle"));
		Files.write(testDirectory.resolve("build.gradle"), Arrays.asList(
			"def service = gradle.sharedServices.registrations.nokeeVersionManagement.service",
			"tasks.register('verify') {",
			"  usesService(service)",
			"  doLast {",
			"    service.get().version",
			"  }",
			"}"
		));
	}

	@Test
	void showsMeaningfulError() {
		final BuildResult result = executer.withTasks("verify").buildAndFail();
		assertThat(result.getOutput(), containsString("Please add the Nokee version to use in a .nokee-version file."));
	}
}
