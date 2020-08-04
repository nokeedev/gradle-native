package dev.nokee.ide.base.internal;

import com.google.common.collect.ImmutableSet;
import dev.nokee.ide.base.IdeProject;
import dev.nokee.ide.base.IdeProjectReference;
import dev.nokee.ide.base.IdeWorkspace;
import org.gradle.api.Buildable;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.api.tasks.TaskProvider;

import javax.annotation.Nullable;
import java.util.Set;

public interface IdeWorkspaceInternal<T extends IdeProjectReference> extends IdeWorkspace<T>, Buildable {
	TaskProvider<? extends Task> getGeneratorTask();

	String getDisplayName();

	@Override
	default TaskDependency getBuildDependencies() {
		return new TaskDependency() {
			@Override
			public Set<? extends Task> getDependencies(@Nullable Task task) {
				return ImmutableSet.of(getGeneratorTask().get());
			}
		};
	}
}
