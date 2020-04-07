package dev.nokee.docs;

import dev.nokee.docs.types.UTType;
import org.gradle.api.Task;
import org.gradle.api.file.Directory;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.internal.Cast;

import javax.inject.Inject;

public abstract class SourceSetFactory {
	@Inject
	protected abstract ObjectFactory getObjects();

	public <T extends UTType> SourceSet<T> newSourceSet(String name, Class<T> type) {
		return Cast.uncheckedCast(getObjects().newInstance(DefaultSourceSet.class, name, type));
	}

	public <T extends UTType> SourceSet<T> newSourceSet(String name, Class<T> type, TaskProvider<? extends Task> generatorTask, Provider<Directory> baseDirectory, String includePattern) {
		return Cast.uncheckedCast(getObjects().newInstance(GeneratedSourceSet.class, name, type, generatorTask, baseDirectory, includePattern));
	}
}
