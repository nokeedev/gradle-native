package dev.nokee.docs;

import dev.nokee.docs.types.UTType;
import org.gradle.api.Task;
import org.gradle.api.file.Directory;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskProvider;

import javax.inject.Inject;
import java.util.Optional;

public abstract class GeneratedSourceSet<T extends UTType> extends DefaultSourceSet<T> {
	private final TaskProvider<? extends Task> generatorTask;

	@Inject
	public GeneratedSourceSet(String name, Class<T> type, TaskProvider<? extends Task> generatorTask, Provider<Directory> baseDirectory, String includePattern) {
		super(name, type);
		this.generatorTask = generatorTask;
		// TODO: builtBy may not be required
		getSource().setDir(baseDirectory).builtBy(generatorTask).include(includePattern);
	}

	@Override
	public Optional<TaskProvider<? extends Task>> getGeneratorTask() {
		return Optional.of(generatorTask);
	}
}
