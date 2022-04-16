/*
 * Copyright 2022 the original author or authors.
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

import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.internal.TaskRegistrationFactory;
import org.gradle.api.Action;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskProvider;

public final class ModelBackedTaskRegistry implements TaskRegistry {
	private final TaskRegistrationFactory factory;
	private final ModelRegistry registry;

	public ModelBackedTaskRegistry(TaskRegistrationFactory factory, ModelRegistry registry) {
		this.factory = factory;
		this.registry = registry;
	}

	public static TaskRegistry newInstance(Project project) {
		return new ModelBackedTaskRegistry(project.getExtensions().getByType(TaskRegistrationFactory.class), project.getExtensions().getByType(ModelRegistry.class));
	}

	@Override
	public TaskProvider<Task> register(String name) throws InvalidUserDataException {
		throw new UnsupportedOperationException();
	}

	@Override
	public TaskProvider<Task> register(String name, Action<? super Task> action) throws InvalidUserDataException {
		throw new UnsupportedOperationException();
	}

	@Override
	public TaskProvider<Task> registerIfAbsent(String name) throws InvalidUserDataException {
		throw new UnsupportedOperationException();
	}

	@Override
	public TaskProvider<Task> registerIfAbsent(String name, Action<? super Task> action) throws InvalidUserDataException {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T extends Task> TaskProvider<T> register(String name, Class<T> type) throws InvalidUserDataException {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T extends Task> TaskProvider<T> register(String name, Class<T> type, Action<? super T> action) throws InvalidUserDataException {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T extends Task> TaskProvider<T> registerIfAbsent(String name, Class<T> type) throws InvalidUserDataException {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T extends Task> TaskProvider<T> registerIfAbsent(String name, Class<T> type, Action<? super T> action) throws InvalidUserDataException {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T extends Task> TaskProvider<T> register(TaskIdentifier<T> identifier) throws InvalidUserDataException {
		return (TaskProvider<T>) registry.register(factory.create(identifier, identifier.getType()).build())
			.as(identifier.getType())
			.asProvider();
	}

	@Override
	public <T extends Task> TaskProvider<T> register(TaskIdentifier<T> identifier, Action<? super T> action) throws InvalidUserDataException {
		return (TaskProvider<T>) registry.register(factory.create(identifier, identifier.getType()).build())
			.as(identifier.getType())
			.configure(action)
			.asProvider();
	}

	@Override
	public <T extends Task> TaskProvider<T> registerIfAbsent(TaskIdentifier<T> identifier) throws InvalidUserDataException {
		return (TaskProvider<T>) registry.register(factory.create(identifier, identifier.getType()).build())
			.as(identifier.getType())
			.asProvider();
	}

	@Override
	public <T extends Task> TaskProvider<T> registerIfAbsent(TaskIdentifier<T> identifier, Action<? super T> action) throws InvalidUserDataException {
		return (TaskProvider<T>) registry.register(factory.create(identifier, identifier.getType()).build())
			.as(identifier.getType())
			.configure(action)
			.asProvider();
	}
}
