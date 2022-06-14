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
import dev.nokee.internal.testing.junit.jupiter.GradleFeatureRequirement;
import dev.nokee.internal.testing.junit.jupiter.RequiresGradleFeature;
import dev.nokee.nvm.fixtures.TestLayout;
import net.nokeedev.testing.junit.jupiter.io.TestDirectory;
import net.nokeedev.testing.junit.jupiter.io.TestDirectoryExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.nio.file.Path;

import static dev.nokee.nvm.GradleRunnerActions.warmConfigurationCache;
import static dev.nokee.nvm.ProjectFixtures.applyAnyNokeePlugin;
import static dev.nokee.nvm.ProjectFixtures.nokeeBuild;
import static dev.nokee.nvm.fixtures.CurrentDotJsonTestUtils.writeCurrentVersionTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

@RequiresGradleFeature(GradleFeatureRequirement.CONFIGURATION_CACHE)
@ExtendWith({TestDirectoryExtension.class, ContextualGradleRunnerParameterResolver.class})
class ConfigurationCacheDuringVersionSeedingFromRemoteServiceFunctionalTest {
	@TestDirectory Path testDirectory;
	GradleRunner executer;

	@BeforeEach
	void setup(GradleRunner runner) throws IOException {
		TestLayout.newBuild(testDirectory).configure(nokeeBuild(applyAnyNokeePlugin()));
		executer = runner.withArgument("--configuration-cache").withArgument(whereCurrentReleaseIs("0.4.0")).withTasks("verify");
		warmConfigurationCache(executer);
	}

	@Test
	void reusesConfigurationCacheAfterNokeeVersionSeedFromRemoteLocation() {
		assertThat(executer.build().getOutput(), containsString("Reusing configuration cache"));
	}

	private String whereCurrentReleaseIs(String version) throws IOException {
		return "-Ddev.nokee.internal.currentRelease.url=" + writeCurrentVersionTo(testDirectory, version).toFile().toURI();
	}
}
