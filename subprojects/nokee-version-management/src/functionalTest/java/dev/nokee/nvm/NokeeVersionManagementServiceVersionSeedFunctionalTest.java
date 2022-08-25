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
import dev.gradleplugins.testscript.TestLayout;
import net.nokeedev.testing.junit.jupiter.io.TestDirectory;
import net.nokeedev.testing.junit.jupiter.io.TestDirectoryExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.nio.file.Path;

import static dev.nokee.internal.testing.FileSystemMatchers.aFile;
import static dev.nokee.internal.testing.FileSystemMatchers.anExistingFile;
import static dev.nokee.internal.testing.FileSystemMatchers.withTextContent;
import static dev.nokee.nvm.NokeeVersionManagementServiceSamples.nokeeBuildChildOfNokeeBuild;
import static dev.nokee.nvm.NokeeVersionManagementServiceSamples.nokeeBuildChildOfNonNokeeBuild;
import static dev.nokee.nvm.NokeeVersionManagementServiceSamples.singleNokeeBuild;
import static dev.nokee.nvm.ProjectFixtures.resolveVersion;
import static dev.nokee.nvm.fixtures.CurrentDotJsonTestUtils.writeCurrentVersionTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

@ExtendWith({TestDirectoryExtension.class, ContextualGradleRunnerParameterResolver.class})
class NokeeVersionManagementServiceVersionSeedFunctionalTest {
	@TestDirectory Path testDirectory;

	@Test
	void writesVersionFileFromSeedVersionWhenNoParentBuild(GradleRunner runner) throws IOException {
		singleNokeeBuild(TestLayout.newBuild(testDirectory)).rootBuild(resolveVersion());
		runner.withTasks("verify").withArgument(whereCurrentReleaseIs("0.4.0")).build();
		assertThat(testDirectory.resolve(".nokee-version"), allOf(anExistingFile(), aFile(withTextContent(is("0.4.0")))));
	}

	@Test
	void doesNotWriteVersionFileOnChildOfNonNokeeBuildWithMissingVersionFile(GradleRunner runner) throws IOException {
		nokeeBuildChildOfNonNokeeBuild(TestLayout.newBuild(testDirectory)).childBuild(resolveVersion());
		runner.withTasks("verify").withArgument(whereCurrentReleaseIs("0.4.0")).buildAndFail();
		assertThat(testDirectory.resolve(".nokee-version"), not(anExistingFile()));
		assertThat(testDirectory.resolve("A/.nokee-version"), not(anExistingFile()));
	}

	@Test
	void writesVersionFileOnlyInTopMostBuild(GradleRunner runner) throws IOException {
		nokeeBuildChildOfNokeeBuild(TestLayout.newBuild(testDirectory)).childBuild(resolveVersion());
		runner.withTasks("verify").withArgument(whereCurrentReleaseIs("0.4.0")).build();
		assertThat(testDirectory.resolve(".nokee-version"), allOf(anExistingFile(), aFile(withTextContent(is("0.4.0")))));
		assertThat(testDirectory.resolve("A/.nokee-version"), not(anExistingFile()));
	}

	@Test
	void doesNotWriteVersionFileWhenBuildOfflineAndNoParentBuild(GradleRunner runner) throws IOException {
		singleNokeeBuild(TestLayout.newBuild(testDirectory)).rootBuild(resolveVersion());
		runner.withTasks("verify").withArgument("--offline").withArgument(whereCurrentReleaseIs("0.4.0")).buildAndFail();
		assertThat(testDirectory.resolve(".nokee-version"), not(anExistingFile()));
	}

	private String whereCurrentReleaseIs(String version) throws IOException {
		return "-Ddev.nokee.internal.currentRelease.url=" + writeCurrentVersionTo(testDirectory, version).toFile().toURI();
	}
}
