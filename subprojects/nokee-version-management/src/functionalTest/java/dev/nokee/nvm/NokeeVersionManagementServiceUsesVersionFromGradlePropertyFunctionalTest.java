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

import dev.gradleplugins.runnerkit.BuildResult;
import dev.gradleplugins.runnerkit.GradleRunner;
import dev.gradleplugins.testscript.TestLayout;
import dev.nokee.internal.testing.junit.jupiter.ContextualGradleRunnerParameterResolver;
import net.nokeedev.testing.junit.jupiter.io.TestDirectory;
import net.nokeedev.testing.junit.jupiter.io.TestDirectoryExtension;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static dev.nokee.nvm.NokeeVersionManagementServiceSamples.nokeeBuildChildOfNokeeBuild;
import static dev.nokee.nvm.NokeeVersionManagementServiceSamples.nokeeBuildChildOfNonNokeeBuild;
import static dev.nokee.nvm.NokeeVersionManagementServiceSamples.singleNokeeBuild;
import static dev.nokee.nvm.ProjectFixtures.expect;
import static dev.nokee.nvm.ProjectFixtures.resolveVersion;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

@ExtendWith({TestDirectoryExtension.class, ContextualGradleRunnerParameterResolver.class})
class NokeeVersionManagementServiceUsesVersionFromGradlePropertyFunctionalTest {
	@TestDirectory Path testDirectory;

	@Test
	void usesVersionFromGradlePropertyAsRootBuild(GradleRunner runner) {
		singleNokeeBuild(TestLayout.newBuild(testDirectory)).rootBuild(expect("1.2.3"));
		runner.withArgument("-Pnokee.version=1.2.3").withTasks("verify").build();
	}

	@Test
	void usesVersionFromGradlePropertyAsChildBuildOfNonNokeeBuild(GradleRunner runner) {
		nokeeBuildChildOfNonNokeeBuild(TestLayout.newBuild(testDirectory)).childBuild(expect("1.2.3"));
		runner.withArgument("-Pnokee.version=1.2.3").withTasks("verify").build();
	}

	@Test
	void usesVersionFromGradlePropertiesOfNokeeRootBuildAsGradleProperty(GradleRunner runner) throws IOException {
		Files.write(testDirectory.resolve("gradle.properties"), "nokee.version=1.2.3".getBytes(UTF_8));
		nokeeBuildChildOfNokeeBuild(TestLayout.newBuild(testDirectory)).childBuild(expect("1.2.3"));
		runner.withTasks("verify").build();
	}

	@Test
	@Disabled
	void warnsWhenVersionIsInGradlePropertiesFileInsideNonNokeeRootBuild(GradleRunner runner) throws IOException {
		Files.write(testDirectory.resolve("gradle.properties"), "nokee.version=1.2.3".getBytes(UTF_8));
		nokeeBuildChildOfNonNokeeBuild(TestLayout.newBuild(testDirectory)).childBuild(resolveVersion());
		final BuildResult result = runner.withTasks("verify").buildAndFail();
		assertThat(result.getOutput(), containsString("It looks like Nokee version is declared in gradle.properties of a non-Nokee enabled build. Please apply 'dev.nokee.nokee-version-management' plugin to '/a/b/c/settings.gradle'."));
	}
}
