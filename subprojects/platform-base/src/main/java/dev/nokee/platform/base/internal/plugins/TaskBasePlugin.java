package dev.nokee.platform.base.internal.plugins;

import dev.nokee.platform.base.internal.tasks.TaskRegistry;
import dev.nokee.platform.base.internal.tasks.TaskRegistryImpl;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class TaskBasePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		val taskRegistry = new TaskRegistryImpl(project.getTasks());
		project.getExtensions().add(TaskRegistry.class, "__NOKEE_taskRegistry", taskRegistry);
	}
}
