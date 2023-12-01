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
import org.gradle.api.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;

import static dev.nokee.model.internal.plugins.ModelBasePlugin.factoryRegistryOf;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.model;
import static dev.nokee.testing.base.GradlePluginTestUtils.pluginId;
import static dev.nokee.testing.base.internal.plugins.TestingBasePlugin.testSuites;

class TestSuiteIntegrationTests {
	Project project = ProjectTestUtils.rootProject();
	MyTestSuiteComponentSpec subject;

	@BeforeEach
	void setup() {
		project.apply(pluginId("dev.nokee.testing-base"));
		model(project, factoryRegistryOf(TestSuiteComponent.class)).registerFactory(MyTestSuiteComponentSpec.class);
		subject = testSuites(project).register("test", MyTestSuiteComponentSpec.class).get();
	}

	@Nested
	class LifecycleTaskTest extends TestSuiteLifecycleTaskTester {
		@Override
		public TestSuiteComponent testSuite() {
			return subject;
		}

		@Override
		public Project project() {
			return project;
		}
	}
}
