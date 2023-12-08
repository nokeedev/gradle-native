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
import dev.nokee.model.internal.ModelElementSupport;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin;
import dev.nokee.testing.base.internal.TestableComponentSpec;
import lombok.val;
import org.gradle.api.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static dev.nokee.internal.testing.GradleProviderMatchers.changesAllowed;
import static dev.nokee.internal.testing.GradleProviderMatchers.changesDisallowed;
import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static dev.nokee.internal.testing.ProjectMatchers.hasPlugin;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.factoryRegistryOf;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.model;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.registryOf;
import static dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin.components;
import static dev.nokee.internal.testing.GradlePluginTestUtils.pluginId;
import static dev.nokee.internal.testing.GradlePluginTestUtils.pluginType;
import static dev.nokee.internal.testing.ThrowableMatchers.callTo;
import static dev.nokee.internal.testing.ThrowableMatchers.doesNotThrowException;
import static dev.nokee.internal.testing.ThrowableMatchers.message;
import static dev.nokee.internal.testing.ThrowableMatchers.throwsException;
import static dev.nokee.testing.base.internal.plugins.TestingBasePlugin.testSuites;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.not;

class TestableComponentSpecIntegrationTests {
	Project project = ProjectTestUtils.rootProject();
	TestableComponentSpec subject;

	@BeforeEach
	void setup() {
		project.apply(pluginType(ComponentModelBasePlugin.class));

		model(project, factoryRegistryOf(Component.class)).registerFactory(MyComponentSpec.class);
		subject = components(project).register("jgek", MyComponentSpec.class).get();
	}

	public static abstract class MyComponentSpec extends ModelElementSupport implements TestableComponentSpec {}

	@Test
	void throwsExceptionWhenTestingBasePluginIsNotApplied() {
		assertThat(project, not(hasPlugin("dev.nokee.testing-base")));
		assertThat(callTo(subject::getTestSuites),
			throwsException(message("Please apply 'dev.nokee.testing-base' plugin before using test suites.")));

		project.apply(pluginId("dev.nokee.testing-base"));
		assertThat(callTo(subject::getTestSuites), doesNotThrowException());
	}

	@Test
	void canRegisterTestSuitesViaComponent() {
		project.apply(pluginId("dev.nokee.testing-base"));
		model(project, factoryRegistryOf(TestSuiteComponent.class)).registerFactory(MyTestSuiteComponentSpec.class);

		// register test suites
		subject.getTestSuites().register("test", MyTestSuiteComponentSpec.class);
		model(project, registryOf(TestSuiteComponent.class)).register(ProjectIdentifier.of(project).child("test"), MyTestSuiteComponentSpec.class);

		assertThat("only registered test suites are accessible via component",
			subject.getTestSuites().get(), contains(named("jgekTest")));
		assertThat("all test suites are accessible via project",
			testSuites(project), contains(named("jgekTest"), named("test")));
	}

	@Test
	void useParentComponentAsTestedComponentOnRegisteredTestSuite() {
		project.apply(pluginId("dev.nokee.testing-base"));
		model(project, factoryRegistryOf(TestSuiteComponent.class)).registerFactory(MyTestSuiteComponentSpec.class);

		val testSuite = subject.getTestSuites().register("test", MyTestSuiteComponentSpec.class).get();
		assertThat(testSuite.getTestedComponent(), providerOf(subject));
	}

	@Test
	void disallowChangesToTestedComponentOnTestableComponentTestSuites() {
		project.apply(pluginId("dev.nokee.testing-base"));
		model(project, factoryRegistryOf(TestSuiteComponent.class)).registerFactory(MyTestSuiteComponentSpec.class);

		val testSuite = subject.getTestSuites().register("test", MyTestSuiteComponentSpec.class).get();

		assertThat(testSuite.getTestedComponent(), changesDisallowed());
	}

	@Test
	void allowChangesToTestedComponentOnAdhocTestSuites() {
		project.apply(pluginId("dev.nokee.testing-base"));
		model(project, factoryRegistryOf(TestSuiteComponent.class)).registerFactory(MyTestSuiteComponentSpec.class);

		val testSuite = testSuites(project).register("test", MyTestSuiteComponentSpec.class).get();

		assertThat(testSuite.getTestedComponent(), changesAllowed());
	}
}
