package dev.nokee.platform.base.internal.tasks;

import org.gradle.api.Action;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;

/**
 * Register tasks to the Gradle {@link TaskContainer} by tracking the ownership of the task.
 */
public interface TaskRegistry {
	TaskProvider<Task> register(String name) throws InvalidUserDataException;
	TaskProvider<Task> register(String name, Action<? super Task> action) throws InvalidUserDataException;
	TaskProvider<Task> registerIfAbsent(String name) throws InvalidUserDataException;
	TaskProvider<Task> registerIfAbsent(String name, Action<? super Task> action) throws InvalidUserDataException;

	<T extends Task> TaskProvider<T> register(String name, Class<T> type) throws InvalidUserDataException;
	<T extends Task> TaskProvider<T> register(String name, Class<T> type, Action<? super T> action) throws InvalidUserDataException;
	<T extends Task> TaskProvider<T> registerIfAbsent(String name, Class<T> type) throws InvalidUserDataException;
	<T extends Task> TaskProvider<T> registerIfAbsent(String name, Class<T> type, Action<? super T> action) throws InvalidUserDataException;

	<T extends Task> TaskProvider<T> register(TaskIdentifier<T> identifier) throws InvalidUserDataException;
	<T extends Task> TaskProvider<T> register(TaskIdentifier<T> identifier, Action<? super T> action) throws InvalidUserDataException;
	<T extends Task> TaskProvider<T> registerIfAbsent(TaskIdentifier<T> identifier) throws InvalidUserDataException;
	<T extends Task> TaskProvider<T> registerIfAbsent(TaskIdentifier<T> identifier, Action<? super T> action) throws InvalidUserDataException;
}
