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
import lombok.val;
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
import static dev.nokee.nvm.fixtures.CurrentDotJsonTestUtils.writeCurrentVersionTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

@ExtendWith({TestDirectoryExtension.class, ContextualGradleRunnerParameterResolver.class})
class NokeeVersionManagementServiceUsesVersionFromCurrentReleaseFunctionalTest {
	@TestDirectory Path testDirectory;
	GradleRunner executer;

	@BeforeEach
	void setup(GradleRunner runner) throws IOException {
		executer = runner.withArgument("-Ddev.nokee.internal.currentRelease.url="
			+ writeCurrentVersionTo(testDirectory, "0.4.2").toFile().toURI());
		plugins(it -> it.id("dev.nokee.nokee-version-management")).writeTo(testDirectory.resolve("settings.gradle"));
	}

	@Test
	void loadsNokeeVersionFromRemoteLocation() throws IOException {
		Files.write(testDirectory.resolve("build.gradle"), Arrays.asList(
			"def service = gradle.sharedServices.registrations.nokeeVersionManagement.service",
			"tasks.register('verify') {",
			"  usesService(service)",
			"  doLast {",
			"    assert service.get().version.toString() == '0.4.2'",
			"  }",
			"}"
		));
		executer.withTasks("verify").build();
	}

	@Test
	void doesNotLoadsNokeeVersionFromRemoteLocationWhenOffline() throws IOException {
		Files.write(testDirectory.resolve("build.gradle"), Arrays.asList(
			"def service = gradle.sharedServices.registrations.nokeeVersionManagement.service",
			"tasks.register('verify') {",
			"  usesService(service)",
			"  doLast {",
			"    service.get().version",
			"  }",
			"}"
		));
		val result = executer.withArgument("--offline").withTasks("verify").buildAndFail();
		assertThat(result.getOutput(), containsString("Please add the Nokee version to use in a .nokee-version file."));
	}

	@Test
	void doesNotLoadsNokeeVersionFromRemoteLocationWhenMalformed() throws IOException {
		Files.write(testDirectory.resolve("current.json"), Arrays.asList("malformed!!"));
		Files.write(testDirectory.resolve("build.gradle"), Arrays.asList(
			"def service = gradle.sharedServices.registrations.nokeeVersionManagement.service",
			"tasks.register('verify') {",
			"  usesService(service)",
			"  doLast {",
			"    service.get().version",
			"  }",
			"}"
		));
		val result = executer.withTasks("verify").buildAndFail();
		assertThat(result.getOutput(), containsString("Please add the Nokee version to use in a .nokee-version file."));
	}

	@Test
	void doesNotLoadsNokeeVersionFromRemoteLocationWhenUnavailable() throws IOException {
		Files.delete(testDirectory.resolve("current.json"));
		Files.write(testDirectory.resolve("build.gradle"), Arrays.asList(
			"def service = gradle.sharedServices.registrations.nokeeVersionManagement.service",
			"tasks.register('verify') {",
			"  usesService(service)",
			"  doLast {",
			"    service.get().version",
			"  }",
			"}"
		));
		val result = executer.withTasks("verify").buildAndFail();
		assertThat(result.getOutput(), containsString("Please add the Nokee version to use in a .nokee-version file."));
	}
}
