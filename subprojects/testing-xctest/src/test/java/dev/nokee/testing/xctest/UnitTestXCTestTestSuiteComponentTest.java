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
package dev.nokee.testing.xctest;

import dev.nokee.internal.testing.AbstractPluginTest;
import dev.nokee.internal.testing.PluginRequirement;
import dev.nokee.internal.testing.TaskMatchers;
import dev.nokee.language.objectivec.internal.plugins.ObjectiveCLanguageBasePlugin;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin;
import dev.nokee.platform.base.testers.ComponentTester;
import dev.nokee.platform.base.testers.DependencyAwareComponentTester;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import dev.nokee.testing.xctest.internal.DefaultUnitTestXCTestTestSuiteComponent;
import org.gradle.api.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static dev.nokee.testing.xctest.internal.plugins.ObjectiveCXCTestTestSuitePlugin.unitTestXCTestTestSuite;
import static org.hamcrest.MatcherAssert.assertThat;

@PluginRequirement.Require(type = ComponentModelBasePlugin.class)
class UnitTestXCTestTestSuiteComponentTest extends AbstractPluginTest implements ComponentTester<DefaultUnitTestXCTestTestSuiteComponent>
	, DependencyAwareComponentTester<NativeComponentDependencies>
{
	private DefaultUnitTestXCTestTestSuiteComponent subject;

	@BeforeEach
	void createSubject() {
		project.getPluginManager().apply(ObjectiveCLanguageBasePlugin.class);
		subject = project.getExtensions().getByType(ModelRegistry.class).register(unitTestXCTestTestSuite("vodo", project)).as(DefaultUnitTestXCTestTestSuiteComponent.class).get();
	}

	@Override
	public DefaultUnitTestXCTestTestSuiteComponent subject() {
		return subject;
	}

	@Nested
	class AssembleTaskTest {
		public Task subject() {
			return project().getTasks().getByName("assembleVodo");
		}

		@Test
		public void hasBuildGroup() {
			assertThat(subject(), TaskMatchers.group("build"));
		}

		@Test
		public void hasDescription() {
			assertThat(subject(), TaskMatchers.description("Assembles the outputs of the XCTest test suite ':vodo'."));
		}
	}
}
