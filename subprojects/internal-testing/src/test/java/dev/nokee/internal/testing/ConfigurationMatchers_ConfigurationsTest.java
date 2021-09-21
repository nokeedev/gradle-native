/*
 * Copyright 2021 the original author or authors.
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
package dev.nokee.internal.testing;

import lombok.val;
import org.gradle.api.Project;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.ConfigurationMatchers.configurations;
import static dev.nokee.internal.testing.ConfigurationMatchers.hasConfiguration;
import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.rootProject;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;

class ConfigurationMatchers_ConfigurationsTest extends AbstractMatcherTest {
	@Override
	protected Matcher<?> createMatcher() {
		return configurations(hasItem(named("foo")));
	}

	private static Project aProjectWithConfigurations() {
		val result = rootProject();
		result.getConfigurations().create("test");
		return result;
	}

	@Test
	void canCheckMatchingConfigurations() {
		assertMatches(configurations(hasItem(named("test"))), aProjectWithConfigurations(),
			"matches project with configuration named 'test'");
	}

	@Test
	void canCheckNonMatchingDependencies() {
		assertDoesNotMatch(configurations(hasItem(named("nonExistent"))), aProjectWithConfigurations(),
			"doesn't match project with configuration named 'nonExistent'");
	}

	@Test
	void checkDescription() {
		assertDescription("project's configurations is a collection containing an object named \"test\"",
			configurations(hasItem(named("test"))));
	}

	@Test
	void checkMismatchDescription() {
		assertMismatchDescription("project's configurations mismatches were: [the object's name was \"test\"]",
			configurations(hasItem(named("nonExistent"))), aProjectWithConfigurations());
	}

	@Test
	void canCheckConfigurationFromProject() {
		assertThat(aProjectWithConfigurations(), hasConfiguration(named("test")));
	}

}
