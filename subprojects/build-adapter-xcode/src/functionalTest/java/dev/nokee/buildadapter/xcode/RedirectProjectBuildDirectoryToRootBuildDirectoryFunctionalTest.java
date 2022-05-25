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
import dev.nokee.internal.testing.junit.jupiter.ContextualGradleRunnerParameterResolver;
import dev.nokee.platform.xcode.EmptyXCProject;
import dev.nokee.platform.xcode.XCWorkspaceElement;
import net.nokeedev.testing.junit.jupiter.io.TestDirectory;
import net.nokeedev.testing.junit.jupiter.io.TestDirectoryExtension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.nio.file.Path;

import static dev.gradleplugins.buildscript.blocks.PluginsBlock.plugins;
import static dev.nokee.buildadapter.xcode.GradleTestSnippets.assertVerifyTask;

@ExtendWith({TestDirectoryExtension.class, ContextualGradleRunnerParameterResolver.class})
class RedirectProjectBuildDirectoryToRootBuildDirectoryFunctionalTest {
	@TestDirectory static Path testDirectory;

	@BeforeAll
	static void setup() throws IOException {
		new XCWorkspaceElement("App", "App", "Lib1", "Pods/Lib1", "Pods/Lib2").writeToProject(testDirectory);
		new EmptyXCProject("App").writeToProject(testDirectory);
		new EmptyXCProject("Lib1").writeToProject(testDirectory);
		new EmptyXCProject("Pods/Lib1").writeToProject(testDirectory);
		new EmptyXCProject("Pods/Lib2").writeToProject(testDirectory);
		plugins(it -> it.id("dev.nokee.xcode-build-adapter")).writeTo(testDirectory.resolve("settings.gradle"));
	}

	@Test
	void redirectsSubprojectBuildDirectoryToRootProjectBuildDirectory(GradleRunner runner) throws IOException {
		assertVerifyTask(
			"project(':').buildDir.absolutePath.replace('\\\\', '/').endsWith('/build')",
			"project(':App').buildDir.absolutePath.replace('\\\\', '/').endsWith('/build/subprojects/App-29bgbf8mfc')",
			"project(':Lib1').buildDir.absolutePath.replace('\\\\', '/').endsWith('/build/subprojects/Lib1-22ji4v30pv3')",
			"project(':Pods').buildDir.absolutePath.replace('\\\\', '/').endsWith('/build/subprojects/Pods-22ji7ch9sql')",
			"project(':Pods:Lib1').buildDir.absolutePath.replace('\\\\', '/').endsWith('/build/subprojects/Lib1-3vq31ktoy17hb')",
			"project(':Pods:Lib2').buildDir.absolutePath.replace('\\\\', '/').endsWith('/build/subprojects/Lib2-3vq31ktoy1z7k')"
		).writeTo(testDirectory.resolve("build.gradle"));
		runner.withTasks("verify").build();
	}
}
