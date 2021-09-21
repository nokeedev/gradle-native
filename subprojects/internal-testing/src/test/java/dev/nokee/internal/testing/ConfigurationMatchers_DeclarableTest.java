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

import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.ConfigurationMatchers.declarable;
import static dev.nokee.internal.testing.utils.ConfigurationTestUtils.testConfiguration;
import static dev.nokee.utils.ConfigurationUtils.configureAsDeclarable;

class ConfigurationMatchers_DeclarableTest extends AbstractMatcherTest {
	@Override
	protected Matcher<?> createMatcher() {
		return declarable();
	}

	@Test
	void canCheckMatchingConfiguration() {
		assertMatches(declarable(), testConfiguration(configureAsDeclarable()),
			"matches declarable configuration");
	}

	@Test
	void canCheckNonMatchingConfiguration() {
		assertDoesNotMatch(declarable(), testConfiguration(),
			"doesn't match legacy configuration");
	}

	@Test
	void checkDescription() {
		assertDescription("a declarable configuration", declarable());
	}

	@Test
	void checkMismatchDescription() {
		assertMismatchDescription("was a legacy configuration", declarable(), testConfiguration());
	}
}
