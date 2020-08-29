package dev.nokee.platform.base.internal.tasks;

import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.internal.DomainObjectIdentifierInternal;
import dev.nokee.platform.base.DomainObjectProvider;
import dev.nokee.model.internal.KnownDomainObjectIdentifier;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;

final class ComponentTasksAdapter implements ComponentTasksInternal {
	private final TaskContainer tasks;
	private final TaskIdentifierFactory identifierFactory;
	private final KnownDomainObjectIdentifier<TaskIdentifier<?>> knownIdentifiers = new KnownDomainObjectIdentifier<>(TaskIdentifier::getTaskName);

	public ComponentTasksAdapter(DomainObjectIdentifierInternal identifier, TaskContainer tasks) {
		this(tasks, TaskIdentifierFactory.childOf(identifier));
	}

	public ComponentTasksAdapter(TaskContainer tasks, TaskIdentifierFactory identifierFactory) {
		this.tasks = tasks;
		this.identifierFactory = identifierFactory;
	}

	@Override
	public <T extends Task> DomainObjectProvider<T> register(TaskName taskName, Class<T> type) {
		val identifier = identifierFactory.create(taskName, type);
		TaskProvider<T> result = knownIdentifiers.withKnownIdentifier(identifier, this::registerToDelegate);
		return new TaskDomainObjectProvider<>(identifier, result);
	}

	@Override
	public <T extends Task> DomainObjectProvider<T> register(TaskName taskName, Class<T> type, Action<? super T> action) {
		val identifier = identifierFactory.create(taskName, type);
		TaskProvider<T> result = knownIdentifiers.withKnownIdentifier(identifier, this::registerToDelegate);
		result.configure(action);
		return new TaskDomainObjectProvider<>(identifier, result);
	}

	private <T extends Task> TaskProvider<T> registerToDelegate(DomainObjectIdentifier knownIdentifier) {
		@SuppressWarnings("unchecked")
		val identifier = (TaskIdentifier<T>) knownIdentifier;
		return tasks.register(identifier.getTaskName(), identifier.getType());
	}

	@Override
	public void configureEach(Action<? super Task> action) {
		tasks.configureEach(knownIdentifiers.onlyKnownIdentifier(action));
	}
}
