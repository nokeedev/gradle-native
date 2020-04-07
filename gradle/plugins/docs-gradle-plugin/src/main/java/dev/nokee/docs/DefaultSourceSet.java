package dev.nokee.docs;

import dev.nokee.docs.types.UTType;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskProvider;

import javax.inject.Inject;
import java.util.Optional;

public abstract class DefaultSourceSet<T extends UTType> implements SourceSet<T> {
	private final String name;
	private final Class<T> type;

	@Inject
	public DefaultSourceSet(String name, Class<T> type) {
		this.name = name;
		this.type = type;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Class<T> getType() {
		return type;
	}

	@Override
	public Optional<TaskProvider<? extends Task>> getGeneratorTask() {
		return Optional.empty();
	}

	@Override
	public <R extends UTType> SourceSet<R> transform(LanguageTransform<T, R> transform) {
		return transform.transform(this);
	}
}
