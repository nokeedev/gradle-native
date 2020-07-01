package dev.nokee.ide.base.internal;

import dev.nokee.ide.base.IdeWorkspace;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskProvider;

public interface IdeWorkspaceInternal extends IdeWorkspace {
	TaskProvider<? extends Task> getGeneratorTask();

	String getDisplayName();
}
