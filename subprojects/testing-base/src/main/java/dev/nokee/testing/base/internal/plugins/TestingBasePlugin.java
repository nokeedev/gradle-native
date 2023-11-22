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

import dev.nokee.model.internal.ModelMapAdapters;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.decorators.ModelDecorator;
import dev.nokee.model.internal.plugins.ModelBasePlugin;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.internal.ModelNodeBackedViewStrategy;
import dev.nokee.platform.base.internal.ViewAdapter;
import dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin;
import dev.nokee.testing.base.TestSuiteComponent;
import dev.nokee.testing.base.internal.ContextualTestSuiteContainer;
import dev.nokee.testing.base.internal.TestSuiteComponentSpec;
import dev.nokee.testing.base.internal.TestableComponentSpec;
import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer;
import org.gradle.api.Named;
import org.gradle.api.Namer;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.reflect.TypeOf;

import static dev.nokee.model.internal.ModelElementActionAdapter.elementWith;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.model;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.objects;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.registryOf;
import static dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin.components;

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

		model(project, objects()).register(model(project).getExtensions().create("testSuites", ModelMapAdapters.ForExtensiblePolymorphicDomainObjectContainer.class, TestSuiteComponent.class, testSuites(project), project.getExtensions().getByType(ModelDecorator.class), ProjectIdentifier.of(project)));

		project.getPlugins().withType(ComponentModelBasePlugin.class).configureEach(__ -> {
			components(project).withType(TestableComponentSpec.class).configureEach(elementWith((identifier, component) -> {
				final Namer<Named> namer = new Named.Namer();
				component.getExtensions().add(TestableComponentSpec.TEST_SUITES_EXTENSION_NAME, project.getObjects().newInstance(ContextualTestSuiteContainer.class, identifier, model(project, registryOf(TestSuiteComponent.class)), new ViewAdapter<>(TestSuiteComponent.class, new ModelNodeBackedViewStrategy(it -> namer.determineName((TestSuiteComponent) it), testSuites(project), project.getProviders(), project.getObjects(), (Runnable) () -> {}, identifier))));
			}));
		});

		model(project, objects()).configureEach(TestSuiteComponentSpec.class, (identifier, testSuite) -> {
			identifier.getParents().filter(it -> it.instanceOf(Component.class)).findFirst().ifPresent(it -> {
				testSuite.getTestedComponent().set(it.getAsProvider(Component.class));
				testSuite.getTestedComponent().disallowChanges();
			});
		});

		project.afterEvaluate(proj -> {
			// Force realize all test suite... until we solve the differing problem.
			testSuites(proj).all(__ -> {});
		});
	}
}
