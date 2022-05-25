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
package dev.nokee.init;

import dev.gradleplugins.runnerkit.BuildResult;
import dev.gradleplugins.runnerkit.GradleRunner;
import dev.nokee.internal.testing.junit.jupiter.ContextualGradleRunnerParameterResolver;
import dev.nokee.internal.testing.junit.jupiter.GradleFeatureRequirement;
import dev.nokee.internal.testing.junit.jupiter.RequiresGradleFeature;
import net.nokeedev.testing.junit.jupiter.io.TestDirectory;
import net.nokeedev.testing.junit.jupiter.io.TestDirectoryExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static dev.gradleplugins.buildscript.blocks.PluginsBlock.plugins;
import static dev.nokee.init.fixtures.DotNokeeVersionTestUtils.writeVersionFileTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

@RequiresGradleFeature(GradleFeatureRequirement.CONFIGURATION_CACHE)
@ExtendWith({TestDirectoryExtension.class, ContextualGradleRunnerParameterResolver.class})
class ConfigurationCacheDetectsChangesToDotNokeeRCFileFunctionalTest {
	@TestDirectory Path testDirectory;
	GradleRunner executer;
	BuildResult result;

	@BeforeEach
	void setup(GradleRunner runner) throws IOException {
		writeVersionFileTo(testDirectory, "0.3.0");
		plugins(it -> it.id("dev.nokee.distributions-management")).writeTo(testDirectory.resolve("settings.gradle"));
		executer = runner.withArgument("verify").withArgument("--configuration-cache");
		Files.write(testDirectory.resolve("build.gradle"), Arrays.asList(
			"plugins {",
			"  id 'dev.nokee.jni-library'", // we must resolve a Nokee plugin so the "version" is marked as used
			"}",
			"tasks.register('verify')"
		));
		result = executer.build();
	}

	@Test
	void reusesConfigurationCacheWhenVersionFileDoesNotChanges() {
		assertThat(executer.build().getOutput(), containsString("Reusing configuration cache"));
	}

	@Test
	void doesNotReuseConfigurationCacheWhenVersionFileChange() throws IOException {
		writeVersionFileTo(testDirectory, "0.4.0");
		assertThat(executer.build().getOutput(), not(containsString("Reusing configuration cache")));
	}
}
