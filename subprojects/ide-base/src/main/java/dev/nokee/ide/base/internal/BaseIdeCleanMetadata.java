package dev.nokee.ide.base.internal;

import com.google.common.collect.ImmutableSet;
import org.gradle.api.Task;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.internal.Describables;
import org.gradle.internal.DisplayName;
import org.gradle.plugins.ide.internal.IdeProjectMetadata;

import java.io.File;
import java.util.Set;

public class BaseIdeCleanMetadata implements IdeProjectMetadata {
	private static final File DUMMY_FILE = new File("");
	private final Provider<? extends Task> cleanTask;

	public BaseIdeCleanMetadata(Provider<? extends Task> cleanTask) {
		this.cleanTask = cleanTask;
	}

	@Override
	public File getFile() {
		return DUMMY_FILE;
	}

	@Override
	public Set<? extends Task> getGeneratorTasks() {
		return ImmutableSet.of(cleanTask.get());
	}

	@Override
	public DisplayName getDisplayName() {
		return Describables.of("Clean");
//		return Describables.withTypeAndName(ideProject.get().getDisplayName(), ideProject.get().getName());
	}
}
