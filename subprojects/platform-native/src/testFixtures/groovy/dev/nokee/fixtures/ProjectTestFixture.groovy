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
package dev.nokee.fixtures

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration

trait ProjectTestFixture {
	abstract Project getProjectUnderTest()

	boolean hasTask(String name) {
		return projectUnderTest.tasks.findByName(name) != null
	}

	boolean hasConfiguration(String name) {
		return projectUnderTest.configurations.findByName(name) != null
	}

	Set<Configuration> getConfigurations() {
		return projectUnderTest.configurations.findAll { !['archives', 'default'].contains(it.name) }
	}

	Set<Task> getTasks() {
		return projectUnderTest.tasks.findAll { !['help', 'tasks', 'components', 'dependencies', 'dependencyInsight', 'dependentComponents', 'init', 'model', 'outgoingVariants', 'projects', 'properties', 'wrapper', 'buildEnvironment', 'nokeeModel'].contains(it.name) }
	}

	void evaluateProject(String because) {
		projectUnderTest.evaluate()
	}

	public <T> T one(Iterable<T> c) {
		assert c.size() == 1
		return c.iterator().next()
	}
}
