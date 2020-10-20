package dev.nokee.platform.base.internal.tasks;

import dev.nokee.model.internal.*;
import dev.nokee.model.internal.ProjectIdentifier;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.Task;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;

import javax.inject.Inject;

public final class TaskRegistryImpl implements TaskRegistry {
	private final ProjectIdentifier projectIdentifier;
	private final DomainObjectEventPublisher eventPublisher;
	private final TaskContainer taskContainer;

	@Inject
	public TaskRegistryImpl(ProjectIdentifier projectIdentifier, DomainObjectEventPublisher eventPublisher, TaskContainer taskContainer) {
		this.projectIdentifier = projectIdentifier;
		this.eventPublisher = eventPublisher;
		this.taskContainer = taskContainer;
	}

	@Override
	public TaskProvider<Task> register(String name) {
		val identifier = TaskIdentifier.of(TaskName.of(name), projectIdentifier);
		return register(identifier);
	}

	@Override
	public TaskProvider<Task> register(String name, Action<? super Task> action) {
		val identifier = TaskIdentifier.of(TaskName.of(name), projectIdentifier);
		return register(identifier, action);
	}

	@Override
	public TaskProvider<Task> registerIfAbsent(String name) {
		val identifier = TaskIdentifier.of(TaskName.of(name), projectIdentifier);
		return findTask(identifier).assertNoMismatch().getOrRegister();
	}

	@Override
	public TaskProvider<Task> registerIfAbsent(String name, Action<? super Task> action) {
		val identifier = TaskIdentifier.of(TaskName.of(name), projectIdentifier);
		return findTask(identifier).assertNoMismatch().getOrRegister(action);
	}

	@Override
	public <T extends Task> TaskProvider<T> register(String name, Class<T> type) {
		val identifier = TaskIdentifier.of(TaskName.of(name), type, projectIdentifier);
		return register(identifier);
	}

	@Override
	public <T extends Task> TaskProvider<T> register(String name, Class<T> type, Action<? super T> action) {
		val identifier = TaskIdentifier.of(TaskName.of(name), type, projectIdentifier);
		return register(identifier, action);
	}

	@Override
	public <T extends Task> TaskProvider<T> registerIfAbsent(String name, Class<T> type) {
		val identifier = TaskIdentifier.of(TaskName.of(name), type, projectIdentifier);
		return findTask(identifier).assertNoMismatch().getOrRegister();
	}

	@Override
	public <T extends Task> TaskProvider<T> registerIfAbsent(String name, Class<T> type, Action<? super T> action) {
		val identifier = TaskIdentifier.of(TaskName.of(name), type, projectIdentifier);
		return findTask(identifier).assertNoMismatch().getOrRegister(action);
	}

	@Override
	public <T extends Task> TaskProvider<T> register(TaskIdentifier<T> identifier) {
		fireDiscoveredEvent(identifier);
		val result = taskContainer.register(identifier.getTaskName(), identifier.getType());
		fireRealizableDiscoveredEvent(identifier, result);
		return result;
	}

	@Override
	public <T extends Task> TaskProvider<T> register(TaskIdentifier<T> identifier, Action<? super T> action) {
		fireDiscoveredEvent(identifier);
		val result = taskContainer.register(identifier.getTaskName(), identifier.getType(), action);
		fireRealizableDiscoveredEvent(identifier, result);
		return result;
	}

	@Override
	public <T extends Task> TaskProvider<T> registerIfAbsent(TaskIdentifier<T> identifier) {
		return findTask(identifier).assertNoMismatch().getOrRegister();
	}

	@Override
	public <T extends Task> TaskProvider<T> registerIfAbsent(TaskIdentifier<T> identifier, Action<? super T> action) {
		return findTask(identifier).assertNoMismatch().getOrRegister(action);
	}

	private void fireDiscoveredEvent(TaskIdentifier<?> identifier) {
		eventPublisher.publish(new DomainObjectDiscovered<>(identifier));
	}

	private void fireRealizableDiscoveredEvent(TaskIdentifier<?> identifier, Provider<?> provider) {
		eventPublisher.publish(new RealizableDomainObjectDiscovered(identifier, new RealizableGradleProvider(identifier, provider, eventPublisher)));
	}

	private <T extends Task> TaskSearchResult<T> findTask(TaskIdentifier<T> identifier) {
		String name = identifier.getTaskName();
		for (val element : taskContainer.getCollectionSchema().getElements()) {
			if (element.getName().equals(name)) {
				return new FoundTaskSearchResult<>(identifier, element.getPublicType().getConcreteClass());
			}
		}
		return new MissingTaskSearchResult<>(identifier);
	}

	private interface TaskSearchResult<T extends Task> {
		TaskSearchResult<T> assertNoMismatch();
		TaskProvider<T> getOrRegister();
		TaskProvider<T> getOrRegister(Action<? super T> action);
	}

	private class FoundTaskSearchResult<T extends Task> implements TaskSearchResult<T> {
		private final TaskIdentifier<T> identifier;
		private final Class<?> actualType;

		public FoundTaskSearchResult(TaskIdentifier<T> identifier, Class<?> actualType) {
			this.identifier = identifier;
			this.actualType = actualType;
		}

		@Override
		public TaskSearchResult<T> assertNoMismatch() {
			if (!identifier.getType().equals(actualType)) {
				if (identifier.getType().equals(Task.class) && actualType.equals(DefaultTask.class)) {
					// it's fine, don't worry about it XD
				} else {
					throw new InvalidUserDataException(String.format("Could not register task '%s': Task type requested (%s) does not match actual type (%s).", identifier.getTaskName(), identifier.getType().getCanonicalName(), actualType.getCanonicalName()));
				}
			}
			return this;
		}

		@Override
		public TaskProvider<T> getOrRegister() {
			return taskContainer.named(identifier.getTaskName(), identifier.getType());
		}

		@Override
		public TaskProvider<T> getOrRegister(Action<? super T> action) {
			// do not pass the action as it's already registered
			return taskContainer.named(identifier.getTaskName(), identifier.getType());
		}
	}

	private class MissingTaskSearchResult<T extends Task> implements TaskSearchResult<T> {
		private final TaskIdentifier<T> identifier;

		public MissingTaskSearchResult(TaskIdentifier<T> identifier) {
			this.identifier = identifier;
		}

		@Override
		public TaskSearchResult<T> assertNoMismatch() {
			// No mismatch, no task found!
			return this;
		}

		@Override
		public TaskProvider<T> getOrRegister() {
			return register(identifier);
		}

		@Override
		public TaskProvider<T> getOrRegister(Action<? super T> action) {
			return register(identifier, action);
		}
	}
}
