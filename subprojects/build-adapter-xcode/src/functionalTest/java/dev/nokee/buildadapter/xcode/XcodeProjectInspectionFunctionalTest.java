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
import dev.gradleplugins.runnerkit.TaskOutcome;
import dev.nokee.internal.testing.junit.jupiter.ContextualGradleRunnerParameterResolver;
import dev.nokee.platform.xcode.XcodeSwiftApp;
import lombok.val;
import net.nokeedev.testing.junit.jupiter.io.TestDirectory;
import net.nokeedev.testing.junit.jupiter.io.TestDirectoryExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.nio.file.Path;

import static dev.gradleplugins.buildscript.blocks.PluginsBlock.plugins;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@ExtendWith({TestDirectoryExtension.class, ContextualGradleRunnerParameterResolver.class})
class XcodeProjectInspectionFunctionalTest {
	@TestDirectory Path testDirectory;

	@BeforeEach
	void appliesPluginUnderTest() throws IOException {
		plugins(it -> it.id("dev.nokee.xcode-build-adapter")).writeTo(testDirectory.resolve("settings.gradle"));
	}

	@Test
	void canInspectXcodeProject(GradleRunner runner) {
		new XcodeSwiftApp().writeToProject(testDirectory);
		val result = runner.withTasks(":inspect", "--format=json").build();
		assertThat(result.task(":inspect").getOutcome(), equalTo(TaskOutcome.SUCCESS));
	}

	@Test
	void canInspectXcodeTarget(GradleRunner runner) {
		new XcodeSwiftApp().writeToProject(testDirectory);
		val result = runner.withTasks(":inspect", "--target=XcodeSwiftApp").build();
		assertThat(result.task(":inspect").getOutcome(), equalTo(TaskOutcome.SUCCESS));
	}
}
