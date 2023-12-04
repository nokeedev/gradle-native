/*
 * Copyright 2020 the original author or authors.
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
package dev.nokee.testing.base.internal.plugins;

import dev.nokee.model.internal.ModelElement;
import dev.nokee.model.internal.ModelMapFactory;
import dev.nokee.model.internal.ModelObject;
import dev.nokee.model.internal.ModelObjectIdentifier;
import dev.nokee.model.internal.ModelObjectIdentifiers;
import dev.nokee.model.internal.names.TaskName;
import dev.nokee.model.internal.plugins.ModelBasePlugin;
import dev.nokee.platform.base.Variant;
import dev.nokee.platform.base.internal.RunnableComponentSpec;
import dev.nokee.testing.base.TestSuiteComponent;
import dev.nokee.testing.base.internal.CheckableComponentSpec;
import dev.nokee.testing.base.internal.HasTestSuiteLifecycleTask;
import dev.nokee.testing.base.internal.TestSuiteComponentSpec;
import dev.nokee.testing.base.internal.rules.TestableComponentCapabilityRule;
import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.reflect.TypeOf;

import java.util.Collections;
import java.util.concurrent.Callable;

import static dev.nokee.model.internal.plugins.ModelBasePlugin.mapOf;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.model;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.objects;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.registryOf;
import static dev.nokee.util.ProviderOfIterableTransformer.toProviderOfIterable;
import static dev.nokee.utils.ProviderUtils.ifPresent;
import static dev.nokee.utils.TaskUtils.configureDependsOn;
import static dev.nokee.utils.TaskUtils.configureVerificationGroup;
import static dev.nokee.utils.TransformerUtils.to;
import static dev.nokee.utils.TransformerUtils.transformEach;

public class TestingBasePlugin implements Plugin<Project> {
	private static final TypeOf<ExtensiblePolymorphicDomainObjectContainer<TestSuiteComponent>> TEST_SUITE_COMPONENT_CONTAINER_TYPE = new TypeOf<ExtensiblePolymorphicDomainObjectContainer<TestSuiteComponent>>() {};

	public static ExtensiblePolymorphicDomainObjectContainer<TestSuiteComponent> testSuites(Project target) {
		return target.getExtensions().getByType(TEST_SUITE_COMPONENT_CONTAINER_TYPE);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void apply(Project project) {
		project.getPluginManager().apply(ModelBasePlugin.class);

		project.getExtensions().add(TEST_SUITE_COMPONENT_CONTAINER_TYPE, "testSuites", project.getObjects().polymorphicDomainObjectContainer(TestSuiteComponent.class));

		model(project).getExtensions().add("testSuites", model(project).getExtensions().getByType(ModelMapFactory.class).create(TestSuiteComponent.class, testSuites(project)));

		new TestableComponentCapabilityRule().execute(project);

		// Register test suite's variant lifecycle task
		model(project, mapOf(Variant.class)).whenElementKnow(HasTestSuiteLifecycleTask.class, identity -> {
			final String testSuiteName = identity.getIdentifier().getName().toString();
			final TaskName lifecycleTaskName = TaskName.of(testSuiteName);
			final ModelObjectIdentifier lifecycleTaskIdentifier = identity.getIdentifier().getParent().child(lifecycleTaskName);
			final ModelObject<Task> lifecycleTask = model(project, registryOf(Task.class)).register(lifecycleTaskIdentifier, Task.class);
			lifecycleTask.configure(configureVerificationGroup());
			lifecycleTask.configure(task -> task.setDescription(String.format("Runs the test suite ':%s'.", task.getName())));
			lifecycleTask.configure(configureDependsOn((Callable<?>) () -> {
				return identity.asProvider().map(to(RunnableComponentSpec.class)).flatMap(it -> it.getRunTask()).map(Collections::singletonList).orElse(Collections.emptyList());
			}));
		});

		// Attach test suite's lifecycle tasks to check task
		model(project, mapOf(TestSuiteComponent.class)).whenElementFinalized(TestSuiteComponentSpec.class, testSuite -> {
			ifPresent(testSuite.getTestedComponent().map(to(CheckableComponentSpec.class)), component -> {
				component.checkedBy(new Callable<Object>() {
					@Override
					public Object call() throws Exception {
						return model(project, objects()).get(HasTestSuiteLifecycleTask.class, it -> ModelObjectIdentifiers.descendantOf(it.getIdentifier(), testSuite.getIdentifier()))
							.map(transformEach(element -> model(project, mapOf(Task.class)).getById(lifecycleTaskOf(element)).asProvider()))
							.flatMap(toProviderOfIterable(project.getObjects()::listProperty))
							.orElse(Collections.emptyList());
					}

					private /*static*/ ModelObjectIdentifier lifecycleTaskOf(ModelElement element) {
						return element.getIdentifier().child(TaskName.lifecycle());
					}
				});
			});
		});

		model(project, mapOf(TestSuiteComponent.class))
			.whenElementFinalized(testSuite -> testSuite.getTestedComponent().disallowChanges());

		project.afterEvaluate(proj -> {
			// Force realize all test suite... until we solve the differing problem.
			testSuites(proj).all(__ -> {});
		});
	}
}
