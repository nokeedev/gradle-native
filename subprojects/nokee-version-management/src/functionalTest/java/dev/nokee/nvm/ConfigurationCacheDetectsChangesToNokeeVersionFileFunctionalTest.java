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
import dev.nokee.internal.testing.junit.jupiter.GradleFeatureRequirement;
import dev.nokee.internal.testing.junit.jupiter.RequiresGradleFeature;
import net.nokeedev.testing.junit.jupiter.io.TestDirectory;
import net.nokeedev.testing.junit.jupiter.io.TestDirectoryExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.nio.file.Path;

import static dev.nokee.internal.testing.GradleConfigurationCacheMatchers.configurationCache;
import static dev.nokee.internal.testing.GradleConfigurationCacheMatchers.recalculated;
import static dev.nokee.internal.testing.GradleConfigurationCacheMatchers.reused;
import static dev.nokee.nvm.GradleRunnerActions.warmConfigurationCache;
import static dev.nokee.nvm.ProjectFixtures.applyAnyNokeePlugin;
import static dev.nokee.nvm.ProjectFixtures.nokeeBuild;
import static dev.nokee.nvm.ProjectFixtures.writeVersionFile;
import static org.hamcrest.MatcherAssert.assertThat;

@RequiresGradleFeature(GradleFeatureRequirement.CONFIGURATION_CACHE)
@ExtendWith({TestDirectoryExtension.class, ContextualGradleRunnerParameterResolver.class})
class ConfigurationCacheDetectsChangesToNokeeVersionFileFunctionalTest {
	@TestDirectory Path testDirectory;
	GradleRunner executer;
	BuildResult result;
	TestLayout layout;

	@BeforeEach
	void setup(GradleRunner runner) throws IOException {
		layout = TestLayout.newBuild(testDirectory).configure(nokeeBuild(applyAnyNokeePlugin().andThen(writeVersionFile("0.3.0"))));
		executer = runner.withArgument("verify").withArgument("--configuration-cache");
		result = warmConfigurationCache(executer);
	}

	@Test
	void reusesConfigurationCacheWhenVersionFileDoesNotChanges() {
		assertThat(executer.build(), configurationCache(reused()));
	}

	@Test
	void doesNotReuseConfigurationCacheWhenVersionFileChange() {
		layout.configure(writeVersionFile("0.4.0"));
		assertThat(executer.build(), configurationCache(recalculated()));
	}
}
