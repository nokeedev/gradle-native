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
package dev.nokee.buildadapter.cocoapods;

import dev.gradleplugins.runnerkit.BuildResult;
import dev.gradleplugins.runnerkit.GradleRunner;
import dev.nokee.internal.testing.junit.jupiter.ContextualGradleRunnerParameterResolver;
import dev.nokee.platform.xcode.XcodeSwiftApp;
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
import static dev.nokee.buildadapter.xcode.GradleTestSnippets.doSomethingVerifyTask;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.io.FileMatchers.anExistingDirectory;
import static org.hamcrest.io.FileMatchers.anExistingFile;

@ExtendWith({TestDirectoryExtension.class, ContextualGradleRunnerParameterResolver.class})
class CocoapodsFunctionalTest {
	GradleRunner executer;
	@TestDirectory Path testDirectory;
	BuildResult result;

	@BeforeEach
	void setup(GradleRunner runner) throws IOException {
		new XcodeSwiftApp().writeToProject(testDirectory.toFile());
		Files.write(testDirectory.resolve("Podfile"), Arrays.asList(
			"platform :ios, '9.0'",
			"target 'XcodeSwiftApp' do",
			"  use_frameworks!",
			"  pod 'Alamofire', '~> 3.0'",
			"end"));
		doSomethingVerifyTask().writeTo(testDirectory.resolve("build.gradle"));
		plugins(it -> it.id("dev.nokee.cocoapods-support")).writeTo(testDirectory.resolve("settings.gradle"));
		executer = runner.withArgument("verify");
		result = executer.build();
	}

	@Test
	void createsNewPodfileLockFile() {
		assertThat(testDirectory.resolve("Podfile.lock").toFile(), anExistingFile());
	}

	@Test
	void createsPodsInstallationDirectory() {
		assertThat(testDirectory.resolve("Pods").toFile(), anExistingDirectory());
	}

	@Test
	void updatesPodfileLockFileOnNextBuild() throws IOException {
		Files.write(testDirectory.resolve("Podfile"), Arrays.asList(
			"platform :ios, '9.0'",
			"target 'XcodeSwiftApp' do",
			"  use_frameworks!",
			"  pod 'Alamofire', '~> 3.0'",
			"  pod 'GoogleAnalytics', '~> 3.1'",
			"end"));
		executer.build();
		assertThat(Files.readAllLines(testDirectory.resolve("Podfile.lock")), hasItems(containsString("GoogleAnalytics (~> 3.1)")));
	}
}
