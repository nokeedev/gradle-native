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
import dev.gradleplugins.testscript.TestLayout;
import net.nokeedev.testing.junit.jupiter.io.TestDirectory;
import net.nokeedev.testing.junit.jupiter.io.TestDirectoryExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.nio.file.Path;

import static dev.nokee.nvm.GradleRunnerActions.warmConfigurationCache;
import static dev.nokee.nvm.ProjectFixtures.applyAnyNokeePlugin;
import static dev.nokee.nvm.ProjectFixtures.nokeeBuild;
import static dev.nokee.nvm.ProjectFixtures.writeVersionFile;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

/**
 * Note that we use the backend repositories, e.g. nokeedev.net, as we need usable repositories.
 */
@RequiresGradleFeature(GradleFeatureRequirement.CONFIGURATION_CACHE)
@ExtendWith({TestDirectoryExtension.class, ContextualGradleRunnerParameterResolver.class})
class ConfigurationCacheDetectsChangesToRepositoryUrlOverridesFunctionalTest {
	@TestDirectory Path testDirectory;
	GradleRunner executer;
	TestLayout layout;

	@BeforeEach
	void setup(GradleRunner runner) throws IOException {
		layout = TestLayout.newBuild(testDirectory).configure(nokeeBuild(applyAnyNokeePlugin()));
		executer = runner.withArgument("--configuration-cache").withTasks("verify");
		warmConfigurationCache(executer);
	}

	@Nested
	class ReleaseRepositoryUrlOverrideTest {
		@BeforeEach
		void setup() throws IOException {
			layout.configure(writeVersionFile("0.4.0"));
			executer = executer.withArgument("-Ddev.nokee.repository.release.url.override=https://repo-release.nokeedev.net");
			warmConfigurationCache(executer);
		}

		@Test
		void reusesConfigurationCacheWhenRepositoryUrlOverrideDoesNotChanges() {
			assertThat(executer.build().getOutput(), containsString("Reusing configuration cache"));
		}

		@Test
		void reusesConfigurationCacheWhenOnlySnapshotRepositoryUrlOverrideChange() throws IOException {
			assertThat(executer.withArgument("-Ddev.nokee.repository.snapshot.url.override=https://my-company.com/snapshot").build().getOutput(), containsString("Reusing configuration cache"));
		}

		@Test
		void doesNotReuseConfigurationCacheWhenRepositoryUrlOverrideChange() throws IOException {
			// Use gradlePluginPortal() URL as all released plugins should be present
			assertThat(executer.withArgument("-Ddev.nokee.repository.release.url.override=https://plugins.gradle.org/m2").build().getOutput(), not(containsString("Reusing configuration cache")));
		}
	}


	@Nested
	class SnapshotRepositoryUrlOverrideTest {
		@BeforeEach
		void setup() throws IOException {
			layout.configure(writeVersionFile("0.4.2190-202205201507.5d969a1e"));
			executer = executer.withArgument("-Ddev.nokee.repository.snapshot.url.override=https://repo-snapshot.nokeedev.net");
			warmConfigurationCache(executer);
		}

		@Test
		void reusesConfigurationCacheWhenRepositoryUrlOverrideDoesNotChanges() {
			assertThat(executer.build().getOutput(), containsString("Reusing configuration cache"));
		}

		@Test
		void reusesConfigurationCacheWhenOnlyReleaseRepositoryUrlOverrideChange() throws IOException {
			assertThat(executer.withArgument("-Ddev.nokee.repository.release.url.override=https://my-company.com/release").build().getOutput(), containsString("Reusing configuration cache"));
		}

		@Test
		void doesNotReuseConfigurationCacheWhenRepositoryUrlOverrideChange() throws IOException {
			// We fake a change to the repo by appending an additional slash. In theory, this should not be a meaningful change.
			assertThat(executer.withArgument("-Ddev.nokee.repository.snapshot.url.override=https://repo-snapshot.nokeedev.net/").build().getOutput(), not(containsString("Reusing configuration cache")));
		}
	}
}
