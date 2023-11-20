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
import dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin;
import dev.nokee.testing.base.TestSuiteComponent;
import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionAware;

import static dev.nokee.model.internal.plugins.ModelBasePlugin.model;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.objects;

public class TestingBasePlugin implements Plugin<Project> {
	private static final org.gradle.api.reflect.TypeOf<ExtensiblePolymorphicDomainObjectContainer<TestSuiteComponent>> TEST_SUITE_COMPONENT_CONTAINER_TYPE = new org.gradle.api.reflect.TypeOf<ExtensiblePolymorphicDomainObjectContainer<TestSuiteComponent>>() {};

	public static ExtensiblePolymorphicDomainObjectContainer<TestSuiteComponent> testSuites(ExtensionAware target) {
		return target.getExtensions().getByType(TEST_SUITE_COMPONENT_CONTAINER_TYPE);
	}

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(ComponentModelBasePlugin.class);

		project.getExtensions().add(TEST_SUITE_COMPONENT_CONTAINER_TYPE, "$testSuites", project.getObjects().polymorphicDomainObjectContainer(TestSuiteComponent.class));

		model(project, objects()).register(model(project).getExtensions().create("testSuites", ModelMapAdapters.ForExtensiblePolymorphicDomainObjectContainer.class, TestSuiteComponent.class, testSuites(project)));

		project.afterEvaluate(proj -> {
			// Force realize all test suite... until we solve the differing problem.
			testSuites(proj).all(__ -> {});
		});
	}
}
