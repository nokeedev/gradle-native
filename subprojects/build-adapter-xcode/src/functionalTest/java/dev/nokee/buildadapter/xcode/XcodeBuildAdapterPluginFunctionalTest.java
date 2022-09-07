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
import net.nokeedev.testing.junit.jupiter.io.TestDirectory;
import net.nokeedev.testing.junit.jupiter.io.TestDirectoryExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.nio.file.Path;

import static dev.gradleplugins.buildscript.blocks.GradleBlock.rootProject;
import static dev.gradleplugins.buildscript.blocks.PluginsBlock.plugins;
import static dev.gradleplugins.buildscript.statements.Statement.block;
import static dev.nokee.buildadapter.xcode.GradleTestSnippets.assertStatements;
import static dev.nokee.buildadapter.xcode.GradleTestSnippets.registerVerifyTask;

@ExtendWith({TestDirectoryExtension.class, ContextualGradleRunnerParameterResolver.class})
public class XcodeBuildAdapterPluginFunctionalTest {
	@TestDirectory Path testDirectory;

	@BeforeEach
	void setup() throws IOException {
		plugins(it -> it.id("dev.nokee.xcode-build-adapter")).writeTo(testDirectory.resolve("settings.gradle"));
	}

	@Test
	void appliesCocoaPodsSupportPlugin(GradleRunner runner) throws IOException {
		block("gradle", rootProject(registerVerifyTask(assertStatements("settings.pluginManager.hasPlugin('dev.nokee.cocoapods-support')")))).useGetter().appendTo(testDirectory.resolve("settings.gradle"));
		runner.withArgument("verify").build();
	}

	@Test
	void appliesModelBasePlugin(GradleRunner runner) throws IOException {
		block("gradle", rootProject(registerVerifyTask(assertStatements("settings.pluginManager.hasPlugin('dev.nokee.model-base')")))).useGetter().appendTo(testDirectory.resolve("settings.gradle"));
		runner.withArgument("verify").build();
	}
}
