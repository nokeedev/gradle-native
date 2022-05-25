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

import dev.gradleplugins.buildscript.blocks.RepositoriesBlock;
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
class PluginManagementDefaultRepositoriesFunctionalTest {
	@TestDirectory Path testDirectory;
	GradleRunner executer;

	@BeforeEach
	void setup(GradleRunner runner) throws IOException {
		executer = runner;
		writeVersionFileTo(testDirectory, "0.5.21");
	}

	@Test
	void usesGradlePluginPortalRepositoryAsFirstRepositoryWhenNoRepositoryPresent() throws IOException {
		plugins(it -> it.id("dev.nokee.distributions-management")).writeTo(testDirectory.resolve("settings.gradle"));

		Files.write(testDirectory.resolve("build.gradle"), Arrays.asList(
			"tasks.register('verify') {",
			"  doLast {",
			"    assert gradle.settings.pluginManagement.repositories.first().url.toString() == 'https://plugins.gradle.org/m2'",
			"  }",
			"}"
		));
		executer.withTasks("verify").build();
	}

	@Test
	void doesNotIncludeGradlePluginPortalRepositoryWhenRepositoriesArePresent() throws IOException {
		SettingsBlock.builder().pluginManagement(it -> it.repositories(RepositoriesBlock.Builder::mavenCentral))
			.plugins(it -> it.id("dev.nokee.distributions-management"))
			.build().writeTo(testDirectory.resolve("settings.gradle"));

		Files.write(testDirectory.resolve("build.gradle"), Arrays.asList(
			"tasks.register('verify') {",
			"  doLast {",
			"    assert !gradle.settings.pluginManagement.repositories.any { it.url.toString() == 'https://plugins.gradle.org/m2' }",
			"  }",
			"}"
		));
		executer.withTasks("verify").build();
	}
}
