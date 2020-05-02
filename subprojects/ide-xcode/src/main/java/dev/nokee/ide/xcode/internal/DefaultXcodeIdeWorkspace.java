package dev.nokee.ide.xcode.internal;

import dev.nokee.ide.xcode.XcodeIdeWorkspace;
import dev.nokee.ide.xcode.internal.tasks.GenerateXcodeIdeWorkspaceTask;
import lombok.Getter;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;

import javax.inject.Inject;

public abstract class DefaultXcodeIdeWorkspace implements XcodeIdeWorkspace {
	@Getter private final TaskProvider<GenerateXcodeIdeWorkspaceTask> generatorTask;

	@Inject
	public DefaultXcodeIdeWorkspace() {
		generatorTask = getTasks().register("xcodeWorkspace", GenerateXcodeIdeWorkspaceTask.class);
	}

	@Inject
	protected abstract TaskContainer getTasks();

	public Provider<FileSystemLocation> getLocation() {
		return generatorTask.flatMap(GenerateXcodeIdeWorkspaceTask::getWorkspaceLocation);
	}

	@Override
	public String getDisplayName() {
		return "Xcode workspace";
	}
}
