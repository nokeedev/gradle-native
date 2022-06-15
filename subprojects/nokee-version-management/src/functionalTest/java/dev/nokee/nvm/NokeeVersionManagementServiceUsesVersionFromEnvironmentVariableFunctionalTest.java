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
import dev.nokee.nvm.fixtures.TestLayout;
import net.nokeedev.testing.junit.jupiter.io.TestDirectory;
import net.nokeedev.testing.junit.jupiter.io.TestDirectoryExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.file.Path;

import static dev.nokee.nvm.NokeeVersionManagementServiceSamples.nokeeBuildChildOfNonNokeeBuild;
import static dev.nokee.nvm.NokeeVersionManagementServiceSamples.singleNokeeBuild;
import static dev.nokee.nvm.ProjectFixtures.expect;

@ExtendWith({TestDirectoryExtension.class, ContextualGradleRunnerParameterResolver.class})
class NokeeVersionManagementServiceUsesVersionFromEnvironmentVariableFunctionalTest {
	@TestDirectory Path testDirectory;

	@Test
	void usesVersionFromEnvironmentVariableAsRootBuild(GradleRunner runner) {
		singleNokeeBuild(TestLayout.newBuild(testDirectory)).rootBuild(expect("1.2.3"));
		runner.withEnvironmentVariable("NOKEE_VERSION", "1.2.3").withTasks("verify").build();
	}

	@Test
	void usesVersionFromEnvironmentVariableAsChildBuildOfNonNokeeBuild(GradleRunner runner) {
		nokeeBuildChildOfNonNokeeBuild(TestLayout.newBuild(testDirectory)).childBuild(expect("1.2.3"));
		runner.withEnvironmentVariable("NOKEE_VERSION", "1.2.3").withTasks("verify").build();
	}
}
