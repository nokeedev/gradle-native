/*
 * Copyright 2023 the original author or authors.
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
package dev.nokee.testing.base;

import dev.nokee.internal.testing.util.ProjectTestUtils;
import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.api.reflect.TypeOf;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static dev.nokee.internal.testing.ProjectMatchers.extensions;
import static dev.nokee.internal.testing.ProjectMatchers.publicType;
import static dev.nokee.internal.testing.GradlePluginTestUtils.pluginId;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;

class TestingBasePluginIntegrationTests {
	Project project = ProjectTestUtils.rootProject();

	@BeforeEach
	void givenProjectWithPluginUnderTest() {
		project.apply(pluginId("dev.nokee.testing-base"));
	}

	@Test
	void hasTestSuitesContainerOnProject() {
		assertThat(project, extensions(hasItem(allOf(
			named("testSuites"),
			publicType(new TypeOf<ExtensiblePolymorphicDomainObjectContainer<TestSuiteComponent>>() {})
		))));
	}
}
