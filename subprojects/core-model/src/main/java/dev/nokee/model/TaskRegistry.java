/*
 * Copyright 2021 the original author or authors.
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
package dev.nokee.model;

import dev.nokee.model.internal.SupportedType;
import dev.nokee.model.internal.SupportedTypes;
import dev.nokee.utils.NamedDomainObjectCollectionUtils;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;

import java.util.Objects;

public final class TaskRegistry implements PolymorphicDomainObjectRegistry<Task>, NamedDomainObjectRegistry<Task> {
	private static final TaskContainerRegistrableTypes registrableTypes = new TaskContainerRegistrableTypes();
	private final TaskContainer container;

	public TaskRegistry(TaskContainer container) {
		this.container = container;
	}

	@Override
	public TaskProvider<Task> register(String name) throws InvalidUserDataException {
		Objects.requireNonNull(name);
		return container.register(name);
	}

	@Override
	public <S extends Task> TaskProvider<S> register(String name, Class<S> type) {
		Objects.requireNonNull(name);
		Objects.requireNonNull(type);
		return container.register(name, type);
	}

	@Override
	public TaskProvider<Task> registerIfAbsent(String name) {
		Objects.requireNonNull(name);
		return (TaskProvider<Task>) NamedDomainObjectCollectionUtils.registerIfAbsent(container, name);
	}

	@Override
	public <S extends Task> TaskProvider<S> registerIfAbsent(String name, Class<S> type) {
		Objects.requireNonNull(name);
		Objects.requireNonNull(type);
		return (TaskProvider<S>) NamedDomainObjectCollectionUtils.registerIfAbsent(container, name, type);
	}

	@Override
	public RegistrableTypes getRegistrableTypes() {
		return registrableTypes;
	}

	@Override
	public RegistrableType getRegistrableType() {
		return registrableTypes;
	}

	@Override
	public String toString() {
		return container + " registry";
	}

	private static final class TaskContainerRegistrableTypes implements RegistrableTypes, RegistrableType {
		private final SupportedType registrableTypes = SupportedTypes.anySubtypeOf(Task.class);

		@Override
		public boolean canRegisterType(Class<?> type) {
			Objects.requireNonNull(type);
			return registrableTypes.supports(type);
		}
	}
}
