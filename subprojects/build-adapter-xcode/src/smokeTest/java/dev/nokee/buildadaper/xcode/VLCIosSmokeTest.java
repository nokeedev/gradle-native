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
package dev.nokee.buildadaper.xcode;

import dev.gradleplugins.runnerkit.BuildResult;
import dev.gradleplugins.runnerkit.GradleRunner;
import dev.nokee.core.exec.CommandLine;
import dev.nokee.internal.testing.junit.jupiter.ContextualGradleRunnerParameterResolver;
import net.nokeedev.testing.junit.jupiter.io.TestDirectory;
import net.nokeedev.testing.junit.jupiter.io.TestDirectoryExtension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.nio.file.Path;

import static dev.gradleplugins.buildscript.blocks.PluginsBlock.plugins;

@EnabledOnOs(OS.MAC)
@ExtendWith({TestDirectoryExtension.class, ContextualGradleRunnerParameterResolver.class})
class VLCIosSmokeTest {
	static GradleRunner executer;
	@TestDirectory(includeSpaces = false) static Path testDirectory;
	static BuildResult result;

	@BeforeAll
	static void setup(GradleRunner runner) throws IOException {
		CommandLine.of("git", "clone", "--depth=1", "https://code.videolan.org/videolan/vlc-ios.git", '.').execute(null, testDirectory).waitFor().assertNormalExitValue();
		CommandLine.of("pod", "install", "--repo-update").execute(null, testDirectory).waitFor().assertNormalExitValue();
		plugins(it -> it.id("dev.nokee.xcode-build-adapter")).writeTo(testDirectory.resolve("settings.gradle"));
		executer = runner.withArgument("VLC-iOS");
		result = executer.build();
	}

	@Test
	void doesNotFails() {
		// nothing to do
	}
}
