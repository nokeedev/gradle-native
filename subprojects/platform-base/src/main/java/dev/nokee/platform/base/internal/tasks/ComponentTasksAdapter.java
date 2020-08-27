package dev.nokee.platform.base.internal.tasks;

import dev.nokee.model.internal.DomainObjectIdentifierInternal;
import dev.nokee.platform.base.DomainObjectProvider;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskContainer;

final class ComponentTasksAdapter implements ComponentTasksInternal {
	private final KnownTaskIdentifierRegistry knownIdentifierRegistry;
	private final TaskContainer tasks;
	private final KnownTaskIdentifiers knownIdentifiers;
	private final TaskIdentifierFactory identifierFactory;

	public ComponentTasksAdapter(DomainObjectIdentifierInternal identifier, TaskContainer tasks, KnownTaskIdentifierRegistry knownIdentifierRegistry) {
		this(tasks, knownIdentifierRegistry, new KnownTaskIdentifiersImpl(identifier, knownIdentifierRegistry), TaskIdentifierFactory.childOf(identifier));
	}

	public ComponentTasksAdapter(DomainObjectIdentifierInternal identifier, TaskContainer tasks, KnownTaskIdentifierRegistry knownIdentifierRegistry, TaskIdentifierFactory identifierFactory) {
		this(tasks, knownIdentifierRegistry, new KnownTaskIdentifiersImpl(identifier, knownIdentifierRegistry), identifierFactory);
	}

	private ComponentTasksAdapter(TaskContainer tasks, KnownTaskIdentifierRegistry knownIdentifierRegistry, KnownTaskIdentifiers knownIdentifiers, TaskIdentifierFactory identifierFactory) {
		this.tasks = tasks;
		this.knownIdentifierRegistry = knownIdentifierRegistry;
		this.knownIdentifiers = knownIdentifiers;
		this.identifierFactory = identifierFactory;
	}

	@Override
	public <T extends Task> DomainObjectProvider<T> register(TaskName taskName, Class<T> type) {
		val identifier = identifierFactory.create(taskName, type);
		knownIdentifierRegistry.add(identifier);
		val result = tasks.register(identifier.getTaskName(), identifier.getType());
		return new TaskDomainObjectProvider<>(identifier, result);
	}

	@Override
	public <T extends Task> DomainObjectProvider<T> register(TaskName taskName, Class<T> type, Action<? super T> action) {
		val identifier = identifierFactory.create(taskName, type);
		knownIdentifierRegistry.add(identifier);
		val result = tasks.register(identifier.getTaskName(), identifier.getType(), action);
		return new TaskDomainObjectProvider<>(identifier, result);
	}

	@Override
	public void configureEach(Action<? super Task> action) {
		tasks.configureEach(new KnownTaskIdentifierActionAdapter(knownIdentifiers, action));
	}
}
