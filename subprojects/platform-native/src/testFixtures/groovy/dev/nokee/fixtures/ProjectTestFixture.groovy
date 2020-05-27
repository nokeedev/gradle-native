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
		return projectUnderTest.tasks.findAll { !['help', 'tasks', 'components', 'dependencies', 'dependencyInsight', 'dependentComponents', 'init', 'model', 'outgoingVariants', 'projects', 'properties', 'wrapper', 'buildEnvironment'].contains(it.name) }
	}

	void evaluateProject(String because) {
		NativeServicesTestFixture.initialize()
		projectUnderTest.evaluate()
	}

	public <T> T one(Iterable<T> c) {
		assert c.size() == 1
		return c.iterator().next()
	}
}
