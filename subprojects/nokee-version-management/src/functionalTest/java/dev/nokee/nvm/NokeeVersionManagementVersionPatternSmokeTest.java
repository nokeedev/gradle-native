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
import dev.nokee.internal.testing.junit.jupiter.GradleAtLeast;
import dev.nokee.nvm.fixtures.TestLayout;
import net.nokeedev.testing.junit.jupiter.io.TestDirectory;
import net.nokeedev.testing.junit.jupiter.io.TestDirectoryExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.file.Path;

import static dev.nokee.nvm.NokeeVersionManagementServiceSamples.singleNokeeBuild;
import static dev.nokee.nvm.ProjectFixtures.applyAnyNokeePlugin;
import static dev.nokee.nvm.ProjectFixtures.writeVersionFile;

@ExtendWith({TestDirectoryExtension.class, ContextualGradleRunnerParameterResolver.class})
class NokeeVersionManagementVersionPatternSmokeTest {
	@TestDirectory Path testDirectory;

	@Test
	@GradleAtLeast("6.9")
	void canLoadVersionFromNokeeServices(GradleRunner runner) {
		singleNokeeBuild(TestLayout.newBuild(testDirectory)).rootBuild(applyAnyNokeePlugin().andThen(writeVersionFile("0.4.+")));
		runner/*.publishBuildScans()*/.withTasks("verify").build(); // manually checked, it resolves a nightly version
		// Although it's somewhat fine, we most likely want to use the concept of latest.release and latest.integration.
		// We could consider supporting 0.4.+ which should mean latest.release under the 0.4 release (latest 0.4 patch release).
		// In theory, it should not be a nightly version
	}
}
