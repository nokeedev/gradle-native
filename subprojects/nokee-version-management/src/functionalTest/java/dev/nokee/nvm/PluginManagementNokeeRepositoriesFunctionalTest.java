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

import dev.gradleplugins.buildscript.io.GradleBuildFile;
import dev.gradleplugins.buildscript.io.GradleSettingsFile;
import dev.gradleplugins.runnerkit.GradleRunner;
import dev.nokee.internal.testing.junit.jupiter.ContextualGradleRunnerParameterResolver;
import net.nokeedev.testing.junit.jupiter.io.TestDirectory;
import net.nokeedev.testing.junit.jupiter.io.TestDirectoryExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.nio.file.Path;

import static dev.gradleplugins.buildscript.syntax.Syntax.groovyDsl;
import static dev.nokee.nvm.fixtures.DotNokeeVersionTestUtils.writeVersionFileTo;

@ExtendWith({TestDirectoryExtension.class, ContextualGradleRunnerParameterResolver.class})
class PluginManagementNokeeRepositoriesFunctionalTest {
	@TestDirectory Path testDirectory;
	GradleRunner executer;
	GradleSettingsFile settingsFile;
	GradleBuildFile buildFile;

	@BeforeEach
	void setup(GradleRunner runner) {
		executer = runner;
		settingsFile = GradleSettingsFile.inDirectory(testDirectory);
		buildFile = GradleBuildFile.inDirectory(testDirectory);
		settingsFile.plugins(it -> it.id("dev.nokee.nokee-version-management"));
	}

	@Test
	void usesReleaseRepositoriesOnReleaseVersion() throws IOException {
		writeVersionFileTo(testDirectory, "0.4.0");
		buildFile.append(groovyDsl(
			"tasks.register('verify') {",
			"  doLast {",
			"    assert gradle.settings.pluginManagement.repositories.any { it.url.toString() == 'https://repo.nokee.dev/release' }",
			"    assert !gradle.settings.pluginManagement.repositories.any { it.url.toString() == 'https://repo.nokee.dev/snapshot' }",
			"  }",
			"}"
		));
		executer.withTasks("verify").build();
	}

	@Test
	void usesSnapshotRepositoriesOnIntegrationVersion() throws IOException {
		writeVersionFileTo(testDirectory, "0.4.2190-202205201507.5d969a1e");
		buildFile.append(groovyDsl(
			"tasks.register('verify') {",
			"  doLast {",
			"    assert !gradle.settings.pluginManagement.repositories.any { it.url.toString() == 'https://repo.nokee.dev/release' }",
			"    assert gradle.settings.pluginManagement.repositories.any { it.url.toString() == 'https://repo.nokee.dev/snapshot' }",
			"  }",
			"}"
		));
		executer.withTasks("verify").build();
	}

	@Test
	void usesSnapshotRepositoriesOnSnapshotVersion() throws IOException {
		writeVersionFileTo(testDirectory, "0.5.0-SNAPSHOT");
		buildFile.append(groovyDsl(
			"tasks.register('verify') {",
			"  doLast {",
			"    assert !gradle.settings.pluginManagement.repositories.any { it.url.toString() == 'https://repo.nokee.dev/release' }",
			"    assert gradle.settings.pluginManagement.repositories.any { it.url.toString() == 'https://repo.nokee.dev/snapshot' }",
			"  }",
			"}"
		));
		executer.withTasks("verify").build();
	}
}
