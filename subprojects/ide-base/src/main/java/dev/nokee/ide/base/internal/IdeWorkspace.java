package dev.nokee.ide.base.internal;

import org.gradle.api.Task;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskProvider;

public interface IdeWorkspace {
	Provider<FileSystemLocation> getLocation();

	TaskProvider<? extends Task> getGeneratorTask();

	String getDisplayName();
}
