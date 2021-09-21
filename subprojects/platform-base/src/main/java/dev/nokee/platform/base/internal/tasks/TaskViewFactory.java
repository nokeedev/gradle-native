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

import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.DomainObjectView;
import dev.nokee.model.internal.AbstractDomainObjectViewFactory;
import org.gradle.api.Task;

import static java.util.Objects.requireNonNull;

public final class TaskViewFactory extends AbstractDomainObjectViewFactory<Task> {
	private final TaskRepository repository;
	private final TaskConfigurer configurer;

	public TaskViewFactory(TaskRepository repository, TaskConfigurer configurer) {
		super(Task.class);
		this.repository = repository;
		this.configurer = configurer;
	}

	@Override
	public <S extends Task> TaskViewImpl<S> create(DomainObjectIdentifier viewOwner, Class<S> elementType) {
		return new TaskViewImpl<>(requireNonNull(viewOwner), elementType, repository, configurer, this);
	}
}
