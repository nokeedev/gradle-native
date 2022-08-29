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
package dev.nokee.buildadapter.xcode;

import dev.gradleplugins.runnerkit.GradleRunner;
import dev.nokee.DualMacosIosFramework;
import dev.nokee.internal.testing.junit.jupiter.ContextualGradleRunnerParameterResolver;
import net.nokeedev.testing.junit.jupiter.io.TestDirectory;
import net.nokeedev.testing.junit.jupiter.io.TestDirectoryExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Path;

import static dev.gradleplugins.buildscript.blocks.PluginsBlock.plugins;
import static dev.nokee.internal.testing.FileSystemMatchers.anExistingDirectory;
import static org.hamcrest.MatcherAssert.assertThat;

@EnabledOnOs(OS.MAC)
@ExtendWith({TestDirectoryExtension.class, ContextualGradleRunnerParameterResolver.class})
class ConfigurationSelectionFunctionalTest {
	@TestDirectory Path testDirectory;

	@BeforeEach
	void beforeEachCreateProject() throws IOException {
		new DualMacosIosFramework().writeToProject(testDirectory.toFile());
		plugins(it -> it.id("dev.nokee.xcode-build-adapter")).writeTo(testDirectory.resolve("settings.gradle"));
	}

	@Test
	void executesForDefaultConfigurationByDefault(GradleRunner runner) {
		runner.withTasks(":DualMacOsIosFramework").build();
		assertThat(testDirectory.resolve("build/derivedData/DualMacOsIosFramework/Build/Products/Release-macosx"), anExistingDirectory());
	}

	@ParameterizedTest
	@ValueSource(strings = {"Debug", "Release"})
	void canSelectSdkToUse(String configurationToSelect, GradleRunner runner) throws IOException {
		runner.withTasks(":DualMacOsIosFramework").withArgument("-Dconfiguration=" + configurationToSelect).build();

		assertThat(testDirectory.resolve("build/derivedData/DualMacOsIosFramework/Build/Products/" + configurationToSelect + "-macosx"), anExistingDirectory());
	}
}
