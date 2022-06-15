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
import lombok.val;
import net.nokeedev.testing.junit.jupiter.io.TestDirectory;
import net.nokeedev.testing.junit.jupiter.io.TestDirectoryExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static dev.nokee.nvm.NokeeVersionManagementServiceSamples.nokeeBuildChildOfNokeeBuild;
import static dev.nokee.nvm.NokeeVersionManagementServiceSamples.nokeeBuildChildOfNonNokeeBuild;
import static dev.nokee.nvm.NokeeVersionManagementServiceSamples.singleNokeeBuild;
import static dev.nokee.nvm.ProjectFixtures.expect;
import static dev.nokee.nvm.ProjectFixtures.resolveVersion;
import static dev.nokee.nvm.ProjectFixtures.withVersion;
import static dev.nokee.nvm.fixtures.CurrentDotJsonTestUtils.writeCurrentVersionTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertAll;

@ExtendWith({TestDirectoryExtension.class, ContextualGradleRunnerParameterResolver.class})
class NokeeVersionManagementServiceUsesVersionFromCurrentReleaseFunctionalTest {
	@TestDirectory Path testDirectory;

	@Test
	void loadsVersionFromRemoteService(GradleRunner runner) throws IOException {
		singleNokeeBuild(TestLayout.newBuild(testDirectory)).rootBuild(expect("2.5.0"));
		runner.withArgument(whereCurrentReleaseIs("2.5.0")).withTasks("verify").build();
	}

	@Test
	void doesNotLoadVersionFromRemoteServiceWhenOffline(GradleRunner runner) throws IOException {
		singleNokeeBuild(TestLayout.newBuild(testDirectory)).rootBuild(resolveVersion());
		val result = runner.withArgument(whereCurrentReleaseIs("0.0.0")).withArgument("--offline")
			.withTasks("verify").buildAndFail();
		assertThat(result.getOutput(), containsString("Please add the Nokee version to use in a .nokee-version file."));
	}

	@Test
	void doesNotLoadVersionFromRemoteServiceWhenMalformed(GradleRunner runner) throws IOException {
		singleNokeeBuild(TestLayout.newBuild(testDirectory)).rootBuild(resolveVersion());
		val result = runner.withArgument(whereCurrentReleaseIsMalformed()).withTasks("verify").buildAndFail();
		assertThat(result.getOutput(), containsString("Please add the Nokee version to use in a .nokee-version file."));
	}

	@Test
	void doesNotLoadVersionFromRemoteServiceWhenUnavailable(GradleRunner runner) {
		singleNokeeBuild(TestLayout.newBuild(testDirectory)).rootBuild(resolveVersion());
		val result = runner.withArgument(whereCurrentReleaseIsUnavailable()).withTasks("verify").buildAndFail();
		assertThat(result.getOutput(), containsString("Please add the Nokee version to use in a .nokee-version file."));
	}

	@Test
	void doesNotLoadVersionFromRemoteServiceWhenNokeeBuildIsChildOfNonNokeeBuildRegardlessOfNetworkStatus(GradleRunner runner) throws IOException {
		nokeeBuildChildOfNonNokeeBuild(TestLayout.newBuild(testDirectory)).childBuild(resolveVersion());
		val executer = runner.withArgument(whereCurrentReleaseIs("0.6.9")).withTasks("verify");
		assertAll(
			() -> assertThat(executer.buildAndFail().getOutput(),
				containsString("Please add the Nokee version to use in a .nokee-version file.")),
			() -> assertThat(executer.withArgument("--offline").buildAndFail().getOutput(),
				containsString("Please add the Nokee version to use in a .nokee-version file."))
		);
	}

	@Test
	void loadsVersionFromRemoteServiceByIgnoringCurrentBuildVersionFileAndMissingParentVersionFile(GradleRunner runner) throws IOException {
		nokeeBuildChildOfNokeeBuild(TestLayout.newBuild(testDirectory))
			.rootBuild(expect("2.5.1"))
			.childBuild(withVersion("2.1.0").andThen(expect("2.5.1")));
		runner.withArgument(whereCurrentReleaseIs("2.5.1")).withTasks("verify").build();
	}

	@Test
	void loadsVersionFromRemoteServiceByIgnoringMissingVersionFileInBothBuilds(GradleRunner runner) throws IOException {
		nokeeBuildChildOfNokeeBuild(TestLayout.newBuild(testDirectory))
			.rootBuild(expect("2.5.3"))
			.childBuild(expect("2.5.3"));
		runner.withArgument(whereCurrentReleaseIs("2.5.3")).withTasks("verify").build();
	}

	@Test
	void doesNotLoadVersionFromRemoteServiceWhenOfflineByIgnoringMissingVersionFileInBothBuilds(GradleRunner runner) throws IOException {
		nokeeBuildChildOfNokeeBuild(TestLayout.newBuild(testDirectory))
			.childBuild(resolveVersion());
		val result = runner.withArgument(whereCurrentReleaseIs("0.0.0")).withArgument("--offline")
			.withTasks("verify").buildAndFail();
		assertThat(result.getOutput(), containsString("Please add the Nokee version to use in a .nokee-version file."));
	}

	@Test
	void doesNotLoadVersionFromRemoteServiceWhenOfflineByIgnoringCurrentBuildVersionFileAndMissingParentVersionFile(GradleRunner runner) throws IOException {
		nokeeBuildChildOfNokeeBuild(TestLayout.newBuild(testDirectory))
			.childBuild(withVersion("2.1.1").andThen(resolveVersion()));
		val result = runner.withArgument(whereCurrentReleaseIs("0.0.0")).withArgument("--offline")
			.withTasks("verify").buildAndFail();
		assertThat(result.getOutput(), containsString("Please add the Nokee version to use in a .nokee-version file."));
	}

	private String whereCurrentReleaseIs(String version) throws IOException {
		return "-Ddev.nokee.internal.currentRelease.url=" + writeCurrentVersionTo(testDirectory, version).toFile().toURI();
	}

	private String whereCurrentReleaseIsMalformed() throws IOException {
		val path = testDirectory.resolve("current-malformed.json");
		Files.write(path, "malformed!!".getBytes(StandardCharsets.UTF_8));
		return "-Ddev.nokee.internal.currentRelease.url=" + path.toFile().toURI();
	}

	private String whereCurrentReleaseIsUnavailable() {
		val path = testDirectory.resolve("current-unavailable.json");
		return "-Ddev.nokee.internal.currentRelease.url=" + path.toFile().toURI();
	}
}
