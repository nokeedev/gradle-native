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
import static dev.nokee.nvm.NokeeVersionManagementServiceSamples.nokeeBuildChildOfNonNokeeBuild;
import static dev.nokee.nvm.NokeeVersionManagementServiceSamples.singleNokeeBuild;
import static dev.nokee.nvm.ProjectFixtures.expect;
import static dev.nokee.nvm.ProjectFixtures.nokeeBuild;
import static dev.nokee.nvm.ProjectFixtures.nonNokeeBuild;
import static dev.nokee.nvm.ProjectFixtures.withVersion;

@ExtendWith({TestDirectoryExtension.class, ContextualGradleRunnerParameterResolver.class})
class NokeeVersionManagementServiceUsesVersionFromEnvironmentVariableFunctionalTest {
	@TestDirectory Path testDirectory;

	@Test
	void ignoresCurrentBuildVersion(GradleRunner runner) {
		nokeeBuildChildOfNonNokeeBuild(TestLayout.newBuild(testDirectory))
			.childBuild(withVersion("0.8.0").andThen(expect("0.5.4")));
		runner.withEnvironmentVariable("NOKEE_VERSION", "0.5.4").withTasks("verify").build();
	}

	@Test
	void ignoresMissingVersionFileOnChildBuild(GradleRunner runner) {
		nokeeBuildChildOfNonNokeeBuild(TestLayout.newBuild(testDirectory)).childBuild(expect("0.3.3"));
		runner.withEnvironmentVariable("NOKEE_VERSION", "0.3.3").withTasks("verify").build();
	}

	@Test
	void ignoresParentBuildVersion(GradleRunner runner) {
		nokeeBuildChildOfNokeeBuild(TestLayout.newBuild(testDirectory))
			.rootBuild(withVersion("0.9.0").andThen(expect("1.5.0")))
			.childBuild(withVersion("0.8.0").andThen(expect("1.5.0")));
		runner.withEnvironmentVariable("NOKEE_VERSION", "1.5.0").withTasks("verify").build();
	}

	@Test
	void ignoresCurrentBuildVersionAndMissingParentBuildVersionFile(GradleRunner runner) {
		nokeeBuildChildOfNokeeBuild(TestLayout.newBuild(testDirectory))
			.rootBuild(expect("1.5.0"))
			.childBuild(withVersion("0.8.0").andThen(expect("1.5.0")));
		runner.withEnvironmentVariable("NOKEE_VERSION", "1.5.0").withTasks("verify").build();
	}

	@Test
	void ignoresMissingCurrentVersionFileAndParentBuildVersion(GradleRunner runner) {
		nokeeBuildChildOfNokeeBuild(TestLayout.newBuild(testDirectory))
			.rootBuild(withVersion("0.9.0").andThen(expect("1.5.0")))
			.childBuild(expect("1.5.0"));
		runner.withEnvironmentVariable("NOKEE_VERSION", "1.5.0").withTasks("verify").build();
	}

	@Test
	void ignoresMissingVersionFile(GradleRunner runner) {
		singleNokeeBuild(TestLayout.newBuild(testDirectory)).rootBuild(expect("0.3.0"));
		runner.withEnvironmentVariable("NOKEE_VERSION", "0.3.0").withTasks("verify").build();
	}

	@Test
	void ignoresVersionFileInFavourOfEnvironmentVariable(GradleRunner runner) {
		singleNokeeBuild(TestLayout.newBuild(testDirectory)).rootBuild(withVersion("0.6.9").andThen(expect("0.7.0")));
		runner.withEnvironmentVariable("NOKEE_VERSION", "0.7.0").withTasks("verify").build();
	}
}
