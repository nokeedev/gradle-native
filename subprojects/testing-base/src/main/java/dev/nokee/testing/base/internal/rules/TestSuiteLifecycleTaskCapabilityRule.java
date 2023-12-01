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

package dev.nokee.testing.base.internal.rules;

import dev.nokee.model.internal.ModelElement;
import dev.nokee.model.internal.ModelElementSupport;
import dev.nokee.model.internal.ModelMap;
import dev.nokee.model.internal.ModelMapAdapters;
import dev.nokee.model.internal.ModelObject;
import dev.nokee.model.internal.ModelObjectIdentifier;
import dev.nokee.model.internal.ModelObjectIdentifiers;
import dev.nokee.model.internal.ModelObjectRegistry;
import dev.nokee.model.internal.names.TaskName;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin;
import dev.nokee.testing.base.TestSuiteComponent;
import dev.nokee.testing.base.internal.CheckableComponentSpec;
import dev.nokee.testing.base.internal.tasks.TestSuiteLifecycleTask;
import org.gradle.api.Action;
import org.gradle.api.Buildable;
import org.gradle.api.DomainObjectCollection;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import static dev.nokee.model.internal.plugins.ModelBasePlugin.mapOf;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.model;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.registryOf;
import static dev.nokee.utils.TaskUtils.configureVerificationGroup;

public final class TestSuiteLifecycleTaskCapabilityRule implements Action<Project> {
	private static final Logger LOGGER = Logging.getLogger(TestSuiteLifecycleTaskCapabilityRule.class);

	private static ModelMap<TestSuiteComponent> testSuites(Project project) {
		return model(project, mapOf(TestSuiteComponent.class));
	}

	private static ModelMap<Task> tasks(Project project) {
		return model(project, mapOf(Task.class));
	}

	private static void components(Project project, Action<? super ModelMap<Component>> action) {
		project.getPlugins().withType(ComponentModelBasePlugin.class).configureEach(__ -> {
			action.execute(model(project, mapOf(Component.class)));
		});
	}

	@Override
	public void execute(Project project) {
		// Register lifecycle task
		testSuites(project).whenElementKnow(registerLifecycleTask(model(project, registryOf(Task.class))));

		// Configures defaults
		tasks(project).configureEach(TestSuiteLifecycleTask.class, dependsOnTestSuiteIfBuildable());
		tasks(project).configureEach(TestSuiteLifecycleTask.class, description());
		tasks(project).configureEach(TestSuiteLifecycleTask.class, configureVerificationGroup());

		// Wire onto checkable components
		components(project, it -> it.configureEach(CheckableComponentSpec.class, checkedByLifecycleTasks(project.getTasks())));
	}

	private static Action<CheckableComponentSpec> checkedByLifecycleTasks(DomainObjectCollection<Task> tasks) {
		return component -> {
			// TODO: discover testSuite
			component.checkedBy((Callable<Object>) () -> {
				return tasks.withType(TestSuiteLifecycleTask.class).stream().filter(task -> {
					return ModelElementSupport.safeAsModelElement(task).map(ModelElement::getIdentifier).map(identifier -> {
						return ModelObjectIdentifiers.descendantOf(identifier, component.getIdentifier());
					}).orElse(false);
				}).collect(Collectors.toList());
			});
		};
	}

	private static Action<TestSuiteLifecycleTask> description() {
		return task -> String.format("Runs the test suite ':%s'.", task.getName());
	}

	private static Action<TestSuiteLifecycleTask> dependsOnTestSuiteIfBuildable() {
		return task -> {
			// TODO: We should attach to variant's run task
			task.dependsOn((Callable<Object>) () -> {
				final TestSuiteComponent testSuite = task.getTestSuite().getOrNull();
				if (testSuite == null) {
					LOGGER.warn("no test suite associated with lifecycle " + task);
				} else if (!(testSuite instanceof Buildable)) {
					LOGGER.warn(testSuite + " is not buildable");
				}
				return testSuite;
			});
		};
	}

	private static Action<ModelMapAdapters.ModelElementIdentity> registerLifecycleTask(ModelObjectRegistry<Task> taskRegistry) {
		return (ModelMapAdapters.ModelElementIdentity identity) -> {
			final String testSuiteName = identity.getIdentifier().getName().toString();
			final TaskName lifecycleTaskName = TaskName.of(testSuiteName);
			final ModelObjectIdentifier lifecycleTaskIdentifier = identity.getIdentifier().getParent().child(lifecycleTaskName);
			final ModelObject<TestSuiteLifecycleTask> lifecycleTask = taskRegistry.register(lifecycleTaskIdentifier, TestSuiteLifecycleTask.class);
			lifecycleTask.configure(task ->
				task.getTestSuite().value(identity.asModelObject(TestSuiteComponent.class).asProvider()).disallowChanges());
		};
	}
}
