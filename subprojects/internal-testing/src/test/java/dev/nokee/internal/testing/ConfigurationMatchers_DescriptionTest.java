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
import org.gradle.api.artifacts.Configuration;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.util.ProjectTestUtils.rootProject;
import static dev.nokee.internal.testing.ConfigurationMatchers.description;
import static org.hamcrest.Matchers.*;

class ConfigurationMatchers_DescriptionTest extends AbstractMatcherTest {
	@Override
	protected Matcher<?> createMatcher() {
		return description(equalTo("Some description"));
	}

	private static Configuration aConfigurationWithDescription() {
		val result = rootProject().getConfigurations().create("test");
		result.setDescription("Test dependencies for project.");
		return result;
	}

	@Test
	void canCheckMatchingConfiguration() {
		assertMatches(description(containsString("dependencies")), aConfigurationWithDescription(), "matches configuration description");
	}

	@Test
	void canCheckNonMatchingConfiguration() {
		assertDoesNotMatch(description(equalTo("Something else...")), aConfigurationWithDescription(),
			"doesn't match configuration description");
	}

	@Test
	void checkDescription() {
		assertDescription("a configuration with description of a string starting with \"Test dependencies\"",
			description(startsWith("Test dependencies")));
	}

	@Test
	void checkMismatchDescription() {
		assertMismatchDescription("configuration description was \"Test dependencies for project.\"",
			description(endsWith("...")), aConfigurationWithDescription());
	}
}
