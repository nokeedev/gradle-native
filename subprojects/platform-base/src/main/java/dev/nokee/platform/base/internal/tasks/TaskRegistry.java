/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
