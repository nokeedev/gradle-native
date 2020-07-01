package dev.nokee.ide.base.internal;

import dev.nokee.ide.base.IdeProject;
import org.gradle.api.Buildable;
import org.gradle.api.Describable;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.api.tasks.TaskProvider;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Set;

public interface IdeProjectInternal extends IdeProject, Describable, Buildable {
	TaskProvider<? extends Task> getGeneratorTask();

	@Override
	default TaskDependency getBuildDependencies() {
		return new TaskDependency() {
			@Override
			public Set<? extends Task> getDependencies(@Nullable Task task) {
				return Collections.singleton(getGeneratorTask().get());
			}
		};
	}
}
