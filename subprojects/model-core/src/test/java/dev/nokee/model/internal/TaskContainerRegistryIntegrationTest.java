package dev.nokee.model.internal;

import org.gradle.api.Task;

import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.rootProject;

class TaskContainerRegistryIntegrationTest extends AbstractDomainObjectContainerRegistryIntegrationTest<Task> {
	@Override
	protected NamedDomainObjectContainerRegistry<Task> createSubject() {
		return new NamedDomainObjectContainerRegistry.TaskContainerRegistry(rootProject().getTasks());
	}

	@Override
	protected Class<Task> getType() {
		return Task.class;
	}
}
