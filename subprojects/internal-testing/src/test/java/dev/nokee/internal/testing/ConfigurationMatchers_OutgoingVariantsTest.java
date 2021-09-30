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

import static dev.nokee.internal.testing.ConfigurationMatchers.outgoingVariants;
import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static dev.nokee.internal.testing.util.ConfigurationTestUtils.testConfiguration;
import static org.hamcrest.Matchers.hasItem;

class ConfigurationMatchers_OutgoingVariantsTest extends AbstractMatcherTest {
	@Override
	protected Matcher<?> createMatcher() {
		return outgoingVariants(hasItem(named("foo")));
	}

	private static Configuration aConfigurationWithOutgoingVariants() {
		val result = testConfiguration();
		result.getOutgoing().getVariants().create("existing");
		return result;
	}

	@Test
	void canCheckMatchingOutgoingVariants() {
		assertMatches(outgoingVariants(hasItem(named("existing"))), aConfigurationWithOutgoingVariants(),
			"matches configuration with outgoing variant named 'existing'");
	}

	@Test
	void canCheckNonMatchingOutgoingVariants() {
		assertDoesNotMatch(outgoingVariants(hasItem(named("absent"))), aConfigurationWithOutgoingVariants(),
			"doesn't match configuration with outgoing variant named 'absent'");
	}

	@Test
	void checkDescription() {
		assertDescription("configuration's outgoing variants is a collection containing an object named \"foo\"",
			outgoingVariants(hasItem(named("foo"))));
	}

	@Test
	void checkMismatchDescription() {
		assertMismatchDescription("configuration's outgoing variants mismatches were: [the object's name was \"existing\"]",
			outgoingVariants(hasItem(named("foo"))), aConfigurationWithOutgoingVariants());
	}
}
