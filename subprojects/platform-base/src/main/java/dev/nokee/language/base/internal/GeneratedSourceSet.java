package dev.nokee.language.base.internal;

import org.gradle.api.Task;
import org.gradle.api.file.ConfigurableFileTree;
import org.gradle.api.file.Directory;
import org.gradle.api.file.FileTree;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskProvider;

import javax.inject.Inject;

import static dev.nokee.language.base.internal.UTTypeUtils.onlyIf;

public abstract class GeneratedSourceSet implements SourceSet {
	private final UTType type;
	private final Provider<Directory> sourceDirectory;
	private final TaskProvider<? extends Task> generatedByTask;
	private final ConfigurableFileTree fileTree = getObjects().fileTree();

	@Inject
	protected abstract ObjectFactory getObjects();

	@Inject
	public GeneratedSourceSet(UTType type, Provider<Directory> sourceDirectory, TaskProvider<? extends Task> generatedByTask) {
		this.type = type;
		this.sourceDirectory = sourceDirectory;
		this.generatedByTask = generatedByTask;
		fileTree.setDir(sourceDirectory).builtBy(generatedByTask).include(onlyIf(type));
	}

	public TaskProvider<? extends Task> getGeneratedByTask() {
		return generatedByTask;
	}

	@Override
	public UTType getType() {
		return type;
	}

	@Override
	public FileTree getAsFileTree() {
		return fileTree;
	}
}
