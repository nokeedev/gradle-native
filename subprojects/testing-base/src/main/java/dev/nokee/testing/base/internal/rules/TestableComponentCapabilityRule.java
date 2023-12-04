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
import dev.nokee.model.internal.ModelMap;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.internal.ModelNodeBackedViewStrategy;
import dev.nokee.platform.base.internal.ViewAdapter;
import dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin;
import dev.nokee.testing.base.TestSuiteComponent;
import dev.nokee.testing.base.internal.ContextualTestSuiteContainer;
import dev.nokee.testing.base.internal.TestSuiteComponentSpec;
import dev.nokee.testing.base.internal.TestableComponentSpec;
import org.gradle.api.Action;
import org.gradle.api.Named;
import org.gradle.api.Namer;
import org.gradle.api.Project;

import java.util.function.BiConsumer;

import static dev.nokee.model.internal.ModelElementAction.withElement;
import static dev.nokee.model.internal.TypeFilteringAction.ofType;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.instantiator;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.mapOf;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.model;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.objects;
import static dev.nokee.testing.base.internal.plugins.TestingBasePlugin.testSuites;

public final class TestableComponentCapabilityRule implements Action<Project> {
	private static void components(Project project, Action<? super ModelMap<Component>> action) {
		project.getPlugins().withType(ComponentModelBasePlugin.class).configureEach(__ -> {
			action.execute(model(project, mapOf(Component.class)));
		});
	}

	@Override
	public void execute(Project project) {
		// Attach test suite containers (contextual)
		components(project, it -> it.configureEach(TestableComponentSpec.class, withElement(attachTestSuiteContainer(project))));

		// Auto wire testedComponent to parent Component
		model(project, objects()).configureEach(ofType(TestSuiteComponentSpec.class, withElement((identifier, testSuite) -> {
			identifier.getParents().filter(it -> it.instanceOf(Component.class)).findFirst().ifPresent(it -> {
				testSuite.getTestedComponent().set(it.safeAs(Component.class));
				testSuite.getTestedComponent().disallowChanges();
			});
		})));
	}

	private static BiConsumer<ModelElement, TestableComponentSpec> attachTestSuiteContainer(Project project) {
		return (element, component) -> {
			final Namer<Named> namer = new Named.Namer();
			component.getExtensions().add(TestableComponentSpec.TEST_SUITES_EXTENSION_NAME, instantiator(project).newInstance(ContextualTestSuiteContainer.class, element.getIdentifier(), new ViewAdapter<>(TestSuiteComponent.class, new ModelNodeBackedViewStrategy(testSuite -> namer.determineName((TestSuiteComponent) testSuite), testSuites(project), project.getProviders(), project.getObjects(), (Runnable) () -> {}, element.getIdentifier()))));
		};
	}
}
