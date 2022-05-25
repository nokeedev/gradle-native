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

import dev.gradleplugins.buildscript.blocks.SettingsBlock;
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
import static dev.nokee.init.fixtures.DotNokeeVersionTestUtils.writeVersionFileTo;

@ExtendWith({TestDirectoryExtension.class, ContextualGradleRunnerParameterResolver.class})
class NokeeManagementServiceUsesVersionFromIncludedBuildTopMostParentFunctionalTest {
	@TestDirectory Path testDirectory;
	GradleRunner executer;

	@BeforeEach
	void setup(GradleRunner runner) throws IOException {
		executer = runner;
		SettingsBlock.builder().plugins(it -> it.id("dev.nokee.nokee-version-management"))
			.includeBuild("build-src")
			.build()
			.writeTo(testDirectory.resolve("settings.gradle"));
		writeVersionFileTo(testDirectory, "0.4.3");
		Files.write(testDirectory.resolve("build.gradle"), Arrays.asList(
			"tasks.register('verify') {",
			"  dependsOn gradle.includedBuild('build-src').task(':verify')",
			"}"
		));

		Files.createDirectory(testDirectory.resolve("build-src"));
		Files.createFile(testDirectory.resolve("build-src/settings.gradle"));
		Files.write(testDirectory.resolve("build-src/build.gradle"), Arrays.asList(
			"tasks.register('verify')"
		));

		Files.createDirectory(testDirectory.resolve("build-src/buildSrc"));
		plugins(it -> it.id("dev.nokee.nokee-version-management")).writeTo(testDirectory.resolve("build-src/buildSrc/settings.gradle"));
		Files.write(testDirectory.resolve("build-src/buildSrc/build.gradle"), Arrays.asList(
			"def service = gradle.sharedServices.registrations.nokeeManagement.service",
			"def verifyTask = tasks.register('verify') {",
			"  usesService(service)",
			"  doLast {",
			"    assert service.get().version.toString() == '0.4.3'",
			"  }",
			"}",
			"tasks.named('build') { finalizedBy(verifyTask) }"
		));
	}

	@Test
	void fetchesNokeeVersionFromParentBuild() {
		executer.withTasks("verify").build();
	}
}
