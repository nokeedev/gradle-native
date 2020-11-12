package dev.nokee.platform.nativebase.internal.rules;

import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.platform.base.internal.VariantIdentifier;
import dev.nokee.platform.base.internal.tasks.*;
import org.gradle.api.Project;
import org.gradle.api.Task;

public interface TaskEntityFixture extends NokeeEntitiesFixture {
	Project getProject();

	default TaskRegistry getTaskRegistry() {
		return new TaskRegistryImpl(ProjectIdentifier.of(getProject()), getEventPublisher(), getProject().getTasks());
	}

	default TaskRepository getTaskRepository() {
		return new TaskRepository(getEventPublisher(), getEntityRealizer(), getProject().getProviders());
	}

	static TaskIdentifier<Task> aTaskOfVariant(String name, VariantIdentifier<?> owner) {
		return TaskIdentifier.of(TaskName.of(name), Task.class, owner);
	}
}
