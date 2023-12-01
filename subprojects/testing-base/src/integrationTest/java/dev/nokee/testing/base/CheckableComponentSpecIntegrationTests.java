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
import dev.nokee.model.internal.names.TaskName;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin;
import dev.nokee.testing.base.internal.CheckableComponentSpec;
import dev.nokee.testing.base.internal.tasks.TestSuiteLifecycleTask;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static dev.nokee.internal.testing.TaskMatchers.dependsOn;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.factoryRegistryOf;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.model;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.registryOf;
import static dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin.components;
import static dev.nokee.testing.base.GradlePluginTestUtils.pluginId;
import static dev.nokee.testing.base.GradlePluginTestUtils.pluginType;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.isA;

public class CheckableComponentSpecIntegrationTests {
	Project project = ProjectTestUtils.rootProject();
	CheckableComponentSpec subject;

	@BeforeEach
	void setup() {
		project.apply(pluginType(ComponentModelBasePlugin.class));

		model(project, factoryRegistryOf(Component.class)).registerFactory(MyCheckableComponentSpec.class);
		subject = components(project).register("lepk", MyCheckableComponentSpec.class).get();
	}

	public static abstract class MyCheckableComponentSpec extends ModelElementSupport implements Component, CheckableComponentSpec {}

	@Test
	void hasAllTestSuiteLifecycleTaskAsDependencies() {
		project.apply(pluginId("dev.nokee.testing-base"));
		model(project, registryOf(Task.class)).register(subject.getIdentifier().child(TaskName.of("test")), TestSuiteLifecycleTask.class);
		model(project, registryOf(Task.class)).register(ProjectIdentifier.of(project).child(TaskName.of("integTest")), TestSuiteLifecycleTask.class);

		assertThat("check task depends on child test suite lifecycle tasks",
			subject.getCheckTask().get(), dependsOn(hasItem(allOf(named("testLepk"), isA(TestSuiteLifecycleTask.class)))));
	}
}
