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
import lombok.val;
import net.nokeedev.testing.junit.jupiter.io.TestDirectory;
import net.nokeedev.testing.junit.jupiter.io.TestDirectoryExtension;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.nio.file.Path;

import static dev.nokee.nvm.NokeeVersionManagementServiceSamples.nokeeBuildChildOfNokeeBuild;
import static dev.nokee.nvm.NokeeVersionManagementServiceSamples.nokeeBuildChildOfNonNokeeBuild;
import static dev.nokee.nvm.NokeeVersionManagementServiceSamples.singleNokeeBuild;
import static dev.nokee.nvm.ProjectFixtures.expect;
import static dev.nokee.nvm.ProjectFixtures.resolveVersion;
import static dev.nokee.nvm.ProjectFixtures.withVersion;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

@ExtendWith({TestDirectoryExtension.class, ContextualGradleRunnerParameterResolver.class})
class NokeeVersionManagementServiceUsesVersionFromNokeeVersionFileFunctionalTest {
	@TestDirectory Path testDirectory;

	@Test
	void usesVersionFromVersionFileAsRootBuild(GradleRunner runner) {
		singleNokeeBuild(TestLayout.newBuild(testDirectory)).rootBuild(withVersion("1.2.3").andThen(expect("1.2.3")));
		runner.withTasks("verify").build();
	}

	@Test
	void usesVersionFromVersionFileAsChildBuildOfNonNokeeBuild(GradleRunner runner) {
		nokeeBuildChildOfNonNokeeBuild(TestLayout.newBuild(testDirectory)).childBuild(withVersion("1.2.3").andThen(expect("1.2.3")));
		runner.withTasks("verify").build();
	}

	@Test
	void ignoresVersionFromVersionFileOfRootNonNokeeBuild(GradleRunner runner) {
		nokeeBuildChildOfNonNokeeBuild(TestLayout.newBuild(testDirectory))
			.rootBuild(withVersion("9.9.9"))
			.childBuild(withVersion("1.2.3").andThen(expect("1.2.3")));
		runner.withTasks("verify").build();
	}

	@Test
	void usesVersionFromVersionFileOfRootNokeeBuild(GradleRunner runner) {
		nokeeBuildChildOfNokeeBuild(TestLayout.newBuild(testDirectory))
			.rootBuild(withVersion("2.3.4"))
			.childBuild(withVersion("1.2.3").andThen(expect("2.3.4")));
		runner.withTasks("verify").build();
	}

	@Test
	@Disabled
	void warnsWhenVersionIsInVersionFileInsideNonNokeeRootBuild(GradleRunner runner) throws IOException {
		nokeeBuildChildOfNonNokeeBuild(TestLayout.newBuild(testDirectory)).rootBuild(withVersion("4.5.6")).childBuild(resolveVersion());
		val result = runner.withTasks("verify").buildAndFail();
		assertThat(result.getOutput(), containsString("It looks like Nokee version is declared in .nokee-version of a non-Nokee enabled build. Please apply 'dev.nokee.nokee-version-management' plugin to '/a/b/c/settings.gradle'."));
	}
}
