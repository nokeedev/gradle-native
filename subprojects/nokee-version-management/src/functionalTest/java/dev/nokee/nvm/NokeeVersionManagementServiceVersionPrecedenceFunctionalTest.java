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

import static dev.nokee.nvm.NokeeVersionManagementServiceSamples.nokeeBuildChildOfNokeeBuild;
import static dev.nokee.nvm.NokeeVersionManagementServiceSamples.singleNokeeBuild;
import static dev.nokee.nvm.ProjectFixtures.expect;
import static dev.nokee.nvm.ProjectFixtures.withVersion;

@ExtendWith({TestDirectoryExtension.class, ContextualGradleRunnerParameterResolver.class})
class NokeeVersionManagementServiceVersionPrecedenceFunctionalTest {
	@TestDirectory Path testDirectory;

	@Test
	void environmentVariableHasPrecedenceOverVersionFile(GradleRunner runner) {
		singleNokeeBuild(TestLayout.newBuild(testDirectory)).rootBuild(withVersion("1.2.3").andThen(expect("2.3.4")));
		runner.withEnvironmentVariable("NOKEE_VERSION", "2.3.4").withTasks("verify").build();
	}

	@Test
	void gradlePropertyHasPrecedenceOverVersionFile(GradleRunner runner) {
		singleNokeeBuild(TestLayout.newBuild(testDirectory)).rootBuild(withVersion("1.2.3").andThen(expect("2.3.4")));
		runner.withArgument("-Pnokee.version=2.3.4").withTasks("verify").build();
	}

	@Test
	void systemPropertyHasPrecedenceOverVersionFile(GradleRunner runner) {
		singleNokeeBuild(TestLayout.newBuild(testDirectory)).rootBuild(withVersion("1.2.3").andThen(expect("2.3.4")));
		runner.withArgument("-Dnokee.version=2.3.4").withTasks("verify").build();
	}

	@Test
	void gradlePropertyHasPrecedenceOverEnvironmentVariable(GradleRunner runner) {
		singleNokeeBuild(TestLayout.newBuild(testDirectory)).rootBuild(expect("2.3.4"));
		runner.withEnvironmentVariable("NOKEE_VERSION", "1.2.3").withArgument("-Pnokee.version=2.3.4").withTasks("verify").build();
	}

	@Test
	void systemPropertyHasPrecedenceOverEnvironmentVariable(GradleRunner runner) {
		singleNokeeBuild(TestLayout.newBuild(testDirectory)).rootBuild(expect("2.3.4"));
		runner.withEnvironmentVariable("NOKEE_VERSION", "1.2.3").withArgument("-Dnokee.version=2.3.4").withTasks("verify").build();
	}

	@Test
	void systemPropertyHasPrecedenceOverGradleProperty(GradleRunner runner) {
		singleNokeeBuild(TestLayout.newBuild(testDirectory)).rootBuild(expect("2.3.4"));
		runner.withArgument("-Pnokee.version=1.2.3").withArgument("-Dnokee.version=2.3.4").withTasks("verify").build();
	}

	@Test
	void parentBuildHasPrecedenceOverVersionFile(GradleRunner runner) {
		nokeeBuildChildOfNokeeBuild(TestLayout.newBuild(testDirectory))
			.rootBuild(withVersion("2.3.4"))
			.childBuild(withVersion("1.2.3").andThen(expect("2.3.4")));
		runner.withTasks("verify").build();
	}
}
