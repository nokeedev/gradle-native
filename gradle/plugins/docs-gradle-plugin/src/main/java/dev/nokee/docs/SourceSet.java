package dev.nokee.docs;

import dev.nokee.docs.types.UTType;
import org.gradle.api.Named;
import org.gradle.api.Task;
import org.gradle.api.file.ConfigurableFileTree;
import org.gradle.api.tasks.TaskProvider;

import java.util.Optional;

public interface SourceSet<T extends UTType> extends Named {
	// TODO: Don't hold your breath on this see comment on LanguageSourceSet under gradle/gradle
	ConfigurableFileTree getSource();

	Class<T> getType();

	// TODO: Until we can create empty TaskProvider
	Optional<TaskProvider<? extends Task>> getGeneratorTask();

	// TODO: maybe Function super or something
	<R extends UTType> SourceSet<R> transform(LanguageTransform<T, R> transform);
}
