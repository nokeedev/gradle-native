package dev.nokee.platform.base.internal.tasks;

import dev.nokee.model.internal.KnownDomainObject;
import dev.nokee.model.internal.KnownDomainObjectFactory;
import dev.nokee.model.internal.TypeAwareDomainObjectIdentifier;
import org.gradle.api.Task;

import javax.inject.Provider;

public final class KnownTaskFactory implements KnownDomainObjectFactory<Task> {
	private final Provider<TaskRepository> repositoryProvider;
	private final Provider<TaskConfigurer> configurerProvider;

	public KnownTaskFactory(Provider<TaskRepository> repositoryProvider, Provider<TaskConfigurer> configurerProvider) {
		this.repositoryProvider = repositoryProvider;
		this.configurerProvider = configurerProvider;
	}

	public <T extends Task> KnownTask<T> create(TaskIdentifier<T> identifier) {
		return new KnownTask<>(identifier, repositoryProvider.get().identified(identifier), configurerProvider.get());
	}

	@Override
	public <S extends Task> KnownDomainObject<S> create(TypeAwareDomainObjectIdentifier<S> identifier) {
		return create((TaskIdentifier<S>) identifier);
	}
}
