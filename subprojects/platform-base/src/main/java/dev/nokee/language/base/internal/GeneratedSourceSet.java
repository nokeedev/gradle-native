package dev.nokee.language.base.internal;

import lombok.AccessLevel;
import lombok.Getter;
import org.gradle.api.Task;
import org.gradle.api.file.ConfigurableFileTree;
import org.gradle.api.file.Directory;
import org.gradle.api.file.FileTree;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskProvider;

import javax.inject.Inject;

import static dev.nokee.language.base.internal.UTTypeUtils.onlyIf;

public class GeneratedSourceSet implements SourceSet {
	private final UTType type;
	private final Provider<Directory> sourceDirectory;
	private final TaskProvider<? extends Task> generatedByTask;
	private final ConfigurableFileTree fileTree;
	private final String name;
	@Getter(AccessLevel.PROTECTED) private final ObjectFactory objects;

	@Inject
	public GeneratedSourceSet(String name, UTType type, Provider<Directory> sourceDirectory, TaskProvider<? extends Task> generatedByTask, ObjectFactory objects) {
		this.name = name;
		this.type = type;
		this.objects = objects;
		this.fileTree = objects.fileTree();
		this.sourceDirectory = sourceDirectory;
		this.generatedByTask = generatedByTask;
		fileTree.setDir(sourceDirectory).builtBy(generatedByTask).include(onlyIf(type).getIncludes());
	}

	public TaskProvider<? extends Task> getGeneratedByTask() {
		return generatedByTask;
	}

	@Override
	public String getName() {
		return name;
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
