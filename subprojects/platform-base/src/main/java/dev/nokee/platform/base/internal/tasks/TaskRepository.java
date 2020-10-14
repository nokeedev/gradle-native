package dev.nokee.platform.base.internal.tasks;

import dev.nokee.model.internal.AbstractRealizableDomainObjectRepository;
import dev.nokee.model.internal.DomainObjectEventPublisher;
import dev.nokee.model.internal.RealizableDomainObjectRealizer;
import org.gradle.api.Task;
import org.gradle.api.provider.ProviderFactory;

import javax.inject.Inject;

public final class TaskRepository extends AbstractRealizableDomainObjectRepository<Task> {
	@Inject
	public TaskRepository(DomainObjectEventPublisher eventPublisher, RealizableDomainObjectRealizer realizer, ProviderFactory providerFactory) {
		super(Task.class, eventPublisher, realizer, providerFactory);
	}
}
