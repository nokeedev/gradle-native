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
package dev.nokee.platform.base.internal.dependencies;

import com.google.common.testing.EqualsTester;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.util.ConfigurationTestUtils.testConfiguration;
import static dev.nokee.platform.base.internal.dependencies.ProjectConfigurationActions.assertConfigured;
import static dev.nokee.platform.base.internal.dependencies.ProjectConfigurationActions.description;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasToString;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ProjectConfigurationActions_DescriptionTest {
	@Test
	void canConfigureDescriptionProvidedByRawString() {
		assertThat(testConfiguration(description("foo")).getDescription(), equalTo("foo"));
	}

	@Test
	void canConfigureDescriptionProvidedBySupplier() {
		assertThat(testConfiguration(description(() -> "bar")).getDescription(), equalTo("bar"));
	}

	@Test
	void ignoresDescriptionMismatch() {
		assertDoesNotThrow(() -> assertConfigured(testConfiguration(description("some description")),
			description("some other description")));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(description("foo"), description("foo"))
			.addEqualityGroup(description(() -> "bar"))
			.addEqualityGroup(description(() -> "far"))
			.addEqualityGroup(description("tar"))
			.testEquals();
	}

	@Test
	void checkToString() {
		assertThat(description("foo"), hasToString("ProjectConfigurationUtils.description(Suppliers.ofInstance(foo))"));
	}
}
