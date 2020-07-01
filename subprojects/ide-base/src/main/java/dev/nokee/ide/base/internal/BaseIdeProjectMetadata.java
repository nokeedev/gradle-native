package dev.nokee.ide.base.internal;

import org.gradle.api.Task;
import org.gradle.api.provider.Provider;
import org.gradle.internal.Describables;
import org.gradle.internal.DisplayName;
import org.gradle.plugins.ide.internal.IdeProjectMetadata;

import java.io.File;
import java.util.Collections;
import java.util.Set;

public class BaseIdeProjectMetadata implements IdeProjectMetadata {
	private final Provider<? extends IdeProjectInternal> ideProject;

	public BaseIdeProjectMetadata(Provider<? extends IdeProjectInternal> ideProject) {
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

	@Override
	public File getFile() {
		return ideProject.get().getLocation().get().getAsFile();
	}
}
