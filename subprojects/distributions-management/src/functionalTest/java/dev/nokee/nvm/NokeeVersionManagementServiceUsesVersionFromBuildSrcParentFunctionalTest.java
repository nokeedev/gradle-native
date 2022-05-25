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
package dev.nokee.nvm;

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
import static dev.nokee.nvm.fixtures.DotNokeeVersionTestUtils.writeVersionFileTo;

@ExtendWith({TestDirectoryExtension.class, ContextualGradleRunnerParameterResolver.class})
class NokeeVersionManagementServiceUsesVersionFromBuildSrcParentFunctionalTest {
	@TestDirectory Path testDirectory;
	GradleRunner executer;

	@BeforeEach
	void setup(GradleRunner runner) throws IOException {
		executer = runner;
		plugins(it -> it.id("dev.nokee.nokee-version-management")).writeTo(testDirectory.resolve("settings.gradle"));
		writeVersionFileTo(testDirectory, "0.4.2");
		Files.write(testDirectory.resolve("build.gradle"), Arrays.asList(
			"tasks.register('verify')"
		));

		Files.createDirectory(testDirectory.resolve("buildSrc"));
		plugins(it -> it.id("dev.nokee.nokee-version-management")).writeTo(testDirectory.resolve("buildSrc/settings.gradle"));
		Files.write(testDirectory.resolve("buildSrc/build.gradle"), Arrays.asList(
			"def service = gradle.sharedServices.registrations.nokeeVersionManagement.service",
			"def verifyTask = tasks.register('verify') {",
			"  usesService(service)",
			"  doLast {",
			"    assert service.get().version.toString() == '0.4.2'",
			"  }",
			"}",
			"tasks.named('build') { finalizedBy(verifyTask) }"
		));
	}

	@Test
	void fetchesNokeeVersionFromMainBuild() {
		executer.withTasks("verify").build();
	}
}
