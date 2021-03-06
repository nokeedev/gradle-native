package dev.nokee.ide.base.internal;

import dev.nokee.ide.base.IdeProject;
import dev.nokee.ide.base.IdeProjectReference;
import org.gradle.api.Task;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.internal.Describables;
import org.gradle.internal.DisplayName;
import org.gradle.plugins.ide.internal.IdeProjectMetadata;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Collections;
import java.util.Set;

public class BaseIdeProjectReference implements IdeProjectMetadata, IdeProjectReference, TaskDependency {
	private final Provider<? extends IdeProjectInternal> ideProject;

	public BaseIdeProjectReference(Provider<? extends IdeProjectInternal> ideProject) {
		this.ideProject = ideProject;
	}

	@Override
	public DisplayName getDisplayName() {
		return Describables.withTypeAndName(ideProject.get().getDisplayName(), ideProject.get().getName());
	}

	@Override
	public Set<? extends Task> getGeneratorTasks() {
		return Collections.singleton(ideProject.get().getGeneratorTask().get());
	}

	public Provider<FileSystemLocation> getLocation() {
		return ideProject.flatMap(IdeProject::getLocation);
	}

	/**
	 * @return the effective project location as a {@link File} instance, never null.
	 * @deprecated use {@link #getLocation()} instead.
	 */
	@Deprecated
	@Override
	public File getFile() {
		return ideProject.get().getLocation().get().getAsFile();
	}

	@Override
	public Set<? extends Task> getDependencies(@Nullable Task task) {
		return getGeneratorTasks();
	}
}
