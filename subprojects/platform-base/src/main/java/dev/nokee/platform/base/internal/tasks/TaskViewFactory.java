package dev.nokee.platform.base.internal.tasks;

import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.DomainObjectView;
import dev.nokee.model.internal.AbstractDomainObjectViewFactory;
import org.gradle.api.Task;

import static java.util.Objects.requireNonNull;

public final class TaskViewFactory extends AbstractDomainObjectViewFactory<Task> {
	private final TaskRepository repository;
	private final TaskConfigurer configurer;

	public TaskViewFactory(TaskRepository repository, TaskConfigurer configurer) {
		super(Task.class);
		this.repository = repository;
		this.configurer = configurer;
	}

	@Override
	public <S extends Task> TaskViewImpl<S> create(DomainObjectIdentifier viewOwner, Class<S> elementType) {
		return new TaskViewImpl<>(requireNonNull(viewOwner), elementType, repository, configurer, this);
	}
}
