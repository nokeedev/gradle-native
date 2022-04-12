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

import dev.gradleplugins.runnerkit.BuildResult;
import dev.gradleplugins.runnerkit.GradleRunner;
import dev.nokee.internal.testing.junit.jupiter.ContextualGradleRunnerParameterResolver;
import dev.nokee.platform.xcode.EmptyXCProject;
import net.nokeedev.testing.junit.jupiter.io.TestDirectory;
import net.nokeedev.testing.junit.jupiter.io.TestDirectoryExtension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.nio.file.Path;

import static dev.gradleplugins.buildscript.blocks.PluginsBlock.plugins;
import static dev.gradleplugins.fixtures.runnerkit.BuildResultMatchers.tasksExecuted;
import static dev.nokee.buildadapter.xcode.GradleTestSnippets.doSomethingVerifyTask;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsStringIgnoringCase;

@ExtendWith({TestDirectoryExtension.class, ContextualGradleRunnerParameterResolver.class})
class MultipleXcodeProjectInRootWithoutWorkspaceFunctionalTest {
	@TestDirectory
	static Path testDirectory;
	static BuildResult result;

	@BeforeAll
	static void setup(GradleRunner runner) throws IOException {
		new EmptyXCProject("App").writeToProject(testDirectory);
		new EmptyXCProject("Lib").writeToProject(testDirectory);
		doSomethingVerifyTask().writeTo(testDirectory.resolve("build.gradle"));
		plugins(it -> it.id("dev.nokee.xcode-build-adapter")).writeTo(testDirectory.resolve("settings.gradle"));
		result = runner.withTasks("verify").build();
	}

	@Test
	void doesNotFailTheBuild() {
		assertThat(result, tasksExecuted(":verify"));
	}

	@Test
	void mentionIssue() {
		assertThat(result.getOutput(), containsStringIgnoringCase("multiple Xcode project were found without workspace"));
	}

	@Test
	void warnXcodeBuildAdapterWillUseSpecificXcodeProject() {
		assertThat(result.getOutput(), containsStringIgnoringCase("'dev.nokee.xcode-build-adapter' will use Xcode project located at '" + testDirectory.resolve("App.xcodeproj") + "'"));
	}

	@Test
	void mentionDocumentationResource() {
		assertThat(result.getOutput(), containsStringIgnoringCase("See https://nokee.fyi/using-xcode-build-adapter for more details."));
	}
}
