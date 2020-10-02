package dev.nokee.platform.base.internal.tasks;

import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;

import javax.inject.Inject;

public final class TaskRegistryImpl implements TaskRegistry {
	private final TaskContainer taskContainer;

	@Inject
	public TaskRegistryImpl(TaskContainer taskContainer) {
		this.taskContainer = taskContainer;
	}

	@Override
	public TaskProvider<Task> register(String name) {
		val result = taskContainer.register(name);
		return result;
	}

	@Override
	public TaskProvider<Task> register(String name, Action<? super Task> action) {
		val result = taskContainer.register(name, action);
		return result;
	}

	@Override
	public TaskProvider<Task> registerIfAbsent(String name) {
		return findTask(name, Task.class).assertNoMismatch().getOrRegister();
	}

	@Override
	public TaskProvider<Task> registerIfAbsent(String name, Action<? super Task> action) {
		return findTask(name, Task.class).assertNoMismatch().getOrRegister(action);
	}

	@Override
	public <T extends Task> TaskProvider<T> register(String name, Class<T> type) {
		val result = taskContainer.register(name, type);
		return result;
	}

	@Override
	public <T extends Task> TaskProvider<T> register(String name, Class<T> type, Action<? super T> action) {
		val result = taskContainer.register(name, type, action);
		return result;
	}

	@Override
	public <T extends Task> TaskProvider<T> registerIfAbsent(String name, Class<T> type) {
		return findTask(name, type).assertNoMismatch().getOrRegister();
	}

	@Override
	public <T extends Task> TaskProvider<T> registerIfAbsent(String name, Class<T> type, Action<? super T> action) {
		return findTask(name, type).assertNoMismatch().getOrRegister(action);
	}

	@Override
	public <T extends Task> TaskProvider<T> register(TaskIdentifier<T> identifier) {
		val result = taskContainer.register(identifier.getTaskName(), identifier.getType());
		return result;
	}

	@Override
	public <T extends Task> TaskProvider<T> register(TaskIdentifier<T> identifier, Action<? super T> action) {
		val result = taskContainer.register(identifier.getTaskName(), identifier.getType(), action);
		return result;
	}

	@Override
	public <T extends Task> TaskProvider<T> registerIfAbsent(TaskIdentifier<T> identifier) {
		return findTask(identifier.getTaskName(), identifier.getType()).assertNoMismatch().getOrRegister();
	}

	@Override
	public <T extends Task> TaskProvider<T> registerIfAbsent(TaskIdentifier<T> identifier, Action<? super T> action) {
		return findTask(identifier.getTaskName(), identifier.getType()).assertNoMismatch().getOrRegister(action);
	}

	private <T extends Task> TaskSearchResult<T> findTask(String name, Class<T> type) {
		for (val element : taskContainer.getCollectionSchema().getElements()) {
			if (element.getName().equals(name)) {
				return new FoundTaskSearchResult<>(name, type, element.getPublicType().getConcreteClass());
			}
		}
		return new MissingTaskSearchResult<>(name, type);
	}

	private interface TaskSearchResult<T extends Task> {
		TaskSearchResult<T> assertNoMismatch();
		TaskProvider<T> getOrRegister();
		TaskProvider<T> getOrRegister(Action<? super T> action);
	}

	private class FoundTaskSearchResult<T extends Task> implements TaskSearchResult<T> {
		private final String name;
		private final Class<T> requestedType;
		private final Class<?> actualType;

		public FoundTaskSearchResult(String name, Class<T> requestedType, Class<?> actualType) {
			this.name = name;
			this.requestedType = requestedType;
			this.actualType = actualType;
		}

		@Override
		public TaskSearchResult<T> assertNoMismatch() {
			if (!requestedType.equals(actualType)) {
				if (requestedType.equals(Task.class) && actualType.equals(DefaultTask.class)) {
					// it's fine, don't worry about it XD
				} else {
					throw new InvalidUserDataException(String.format("Could not register task '%s': Task type requested (%s) does not match actual type (%s).", name, requestedType.getCanonicalName(), actualType.getCanonicalName()));
				}
			}
			return this;
		}

		@Override
		public TaskProvider<T> getOrRegister() {
			return taskContainer.named(name, requestedType);
		}

		@Override
		public TaskProvider<T> getOrRegister(Action<? super T> action) {
			// do not pass the action as it's already registered
			return taskContainer.named(name, requestedType);
		}
	}

	private class MissingTaskSearchResult<T extends Task> implements TaskSearchResult<T> {
		private final String name;
		private final Class<T> type;

		public MissingTaskSearchResult(String name, Class<T> type) {
			this.name = name;
			this.type = type;
		}

		@Override
		public TaskSearchResult<T> assertNoMismatch() {
			// No mismatch, no task found!
			return this;
		}

		@Override
		public TaskProvider<T> getOrRegister() {
			return taskContainer.register(name, type);
		}

		@Override
		public TaskProvider<T> getOrRegister(Action<? super T> action) {
			return taskContainer.register(name, type, action);
		}
	}
}
