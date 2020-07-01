package dev.nokee.ide.visualstudio.internal;

import dev.nokee.ide.base.internal.IdeWorkspaceInternal;
import dev.nokee.ide.visualstudio.VisualStudioIdeProject;
import dev.nokee.ide.visualstudio.VisualStudioIdeSolution;
import dev.nokee.ide.visualstudio.internal.tasks.GenerateVisualStudioIdeSolutionTask;
import lombok.Getter;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;

import javax.inject.Inject;

public abstract class DefaultVisualStudioIdeSolution implements VisualStudioIdeSolution, IdeWorkspaceInternal<VisualStudioIdeProject> {
	@Getter private final TaskProvider<GenerateVisualStudioIdeSolutionTask> generatorTask;

	@Inject
	public DefaultVisualStudioIdeSolution() {
		generatorTask = getTasks().register("visualStudioSolution", GenerateVisualStudioIdeSolutionTask.class);
	}

	@Inject
	protected abstract TaskContainer getTasks();

	@Override
	public Provider<FileSystemLocation> getLocation() {
		return generatorTask.flatMap(GenerateVisualStudioIdeSolutionTask::getSolutionLocation);
	}

	@Override
	public String getDisplayName() {
		return "Visual Studio solution";
	}
}
