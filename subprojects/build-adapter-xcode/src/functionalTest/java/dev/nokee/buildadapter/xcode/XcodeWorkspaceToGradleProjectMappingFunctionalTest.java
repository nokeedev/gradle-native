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
import dev.nokee.platform.xcode.EmptyXCProject;
import dev.nokee.platform.xcode.XCWorkspaceElement;
import net.nokeedev.testing.junit.jupiter.io.TestDirectory;
import net.nokeedev.testing.junit.jupiter.io.TestDirectoryExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.nio.file.Path;

import static dev.gradleplugins.buildscript.blocks.PluginsBlock.plugins;
import static dev.nokee.buildadapter.xcode.GradleTestSnippets.assertVerifyTask;

@ExtendWith({TestDirectoryExtension.class, ContextualGradleRunnerParameterResolver.class})
class XcodeWorkspaceToGradleProjectMappingFunctionalTest {
	GradleRunner executer;
	@TestDirectory Path testDirectory;

	@BeforeEach
	void setup(GradleRunner runner) throws IOException {
		plugins(it -> it.id("dev.nokee.xcode-build-adapter")).writeTo(testDirectory.resolve("settings.gradle"));
		executer = runner.withArgument("verify");
	}

	@Test
	void canIncludeSingleXcodeProject() throws IOException {
		new XCWorkspaceElement("App", "App").writeToProject(testDirectory);
		new EmptyXCProject("App").writeToProject(testDirectory);
		assertVerifyTask(
			"project.allprojects.size() == 2", // rootProject + :App
			"project.allprojects.stream().anyMatch { it.path.equals(\":App\") }"
		).writeTo(testDirectory.resolve("build.gradle"));
		executer.build();
	}

	@Test
	void canIncludeMultipleXcodeProjects() throws IOException {
		new XCWorkspaceElement("App", "App", "Lib").writeToProject(testDirectory);
		new EmptyXCProject("App").writeToProject(testDirectory);
		new EmptyXCProject("Lib").writeToProject(testDirectory);
		assertVerifyTask(
			"project.allprojects.size() == 3", // rootProject + :App + :Lib
			"project.allprojects.stream().anyMatch { it.path.equals(\":App\") }",
			"project.allprojects.stream().anyMatch { it.path.equals(\":Lib\") }"
		).writeTo(testDirectory.resolve("build.gradle"));
		executer.build();
	}

	@Test
	void canIncludeNestedXcodeProjects() throws IOException {
		new XCWorkspaceElement("App", "App", "libs/Common").writeToProject(testDirectory);
		new EmptyXCProject("App").writeToProject(testDirectory);
		new EmptyXCProject("Common").writeToProject(testDirectory.resolve("libs"));
		assertVerifyTask(
			"project.allprojects.size() == 4", // rootProject + :App + :libs + :libs:Common
			"project.allprojects.stream().anyMatch { it.path.equals(\":App\") }",
			"project.allprojects.stream().anyMatch { it.path.equals(\":libs:Common\") }"
		).writeTo(testDirectory.resolve("build.gradle"));
		executer.build();
	}
}
