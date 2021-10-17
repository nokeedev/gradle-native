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
import lombok.val;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static dev.nokee.internal.testing.util.ConfigurationTestUtils.testConfiguration;
import static dev.nokee.platform.base.internal.dependencies.ProjectConfigurationActions.assertConfigured;
import static dev.nokee.platform.base.internal.dependencies.ProjectConfigurationActions.extendsFrom;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProjectConfigurationActions_ExtendsFromTest {
	@Test
	void canConfigureExtendsFromSingleConfiguration() {
		assertThat(testConfiguration(extendsFrom(testConfiguration("foo"))).getExtendsFrom(),
			hasItem(named("foo")));
	}

	@Test
	void canConfigureExtendsFromMultipleConfiguration() {
		assertThat(testConfiguration(extendsFrom(testConfiguration("c0"), testConfiguration("c1"))).getExtendsFrom(),
			hasItems(named("c0"), named("c1")));
	}

	@Test
	void canConfigureExtendsFromUsingMultipleActions() {
		assertThat(testConfiguration(extendsFrom(testConfiguration("c2")).andThen(extendsFrom(testConfiguration("c3")))).getExtendsFrom(),
			hasItems(named("c2"), named("c3")));
	}

	@Test
	void throwsExceptionWhenConfigurationIsMissingParentConfiguration() {
		val c0 = testConfiguration("c0");
		val c1 = testConfiguration("c1");
		val ex = assertThrows(IllegalStateException.class, () -> assertConfigured(testConfiguration(), extendsFrom(c0, c1)));
		assertThat(ex.getMessage(), equalTo("Missing parent configuration: c0, c1"));
	}

	@Test
	void doesNotThrowExceptionWhenConfigurationHasSameParentConfiguration() {
		val c0 = testConfiguration("c0");
		val c1 = testConfiguration("c1");
		assertDoesNotThrow(() -> assertConfigured(testConfiguration().extendsFrom(c0, c1), extendsFrom(c0, c1)));
	}

	@Test
	void doesNotThrowExceptionWhenConfigurationHasAdditionalParentConfiguration() {
		val c0 = testConfiguration("c0");
		val c1 = testConfiguration("c1");
		assertDoesNotThrow(() -> assertConfigured(testConfiguration().extendsFrom(c0, c1, testConfiguration("c2")),
			extendsFrom(c0, c1)));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		val c0 = testConfiguration("c0");
		val c1 = testConfiguration("c1");
		val c2 = testConfiguration("c2");
		new EqualsTester()
			.addEqualityGroup(extendsFrom(c0), extendsFrom(c0))
			.addEqualityGroup(extendsFrom(c1))
			.addEqualityGroup(extendsFrom(c0, c1))
			.testEquals();
	}

	@Test
	void checkToString() {
		assertThat(extendsFrom(testConfiguration()), hasToString("ProjectConfigurationUtils.extendsFrom([configuration ':test'])"));
	}
}
