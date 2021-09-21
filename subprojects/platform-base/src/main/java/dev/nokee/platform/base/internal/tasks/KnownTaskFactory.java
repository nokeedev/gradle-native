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

import dev.nokee.model.internal.KnownDomainObject;
import dev.nokee.model.internal.KnownDomainObjectFactory;
import dev.nokee.model.internal.TypeAwareDomainObjectIdentifier;
import org.gradle.api.Task;

import javax.inject.Provider;

public final class KnownTaskFactory implements KnownDomainObjectFactory<Task> {
	private final Provider<TaskRepository> repositoryProvider;
	private final Provider<TaskConfigurer> configurerProvider;

	public KnownTaskFactory(Provider<TaskRepository> repositoryProvider, Provider<TaskConfigurer> configurerProvider) {
		this.repositoryProvider = repositoryProvider;
		this.configurerProvider = configurerProvider;
	}

	public <T extends Task> KnownTask<T> create(TaskIdentifier<T> identifier) {
		return new KnownTask<>(identifier, repositoryProvider.get().identified(identifier), configurerProvider.get());
	}

	@Override
	public <S extends Task> KnownDomainObject<S> create(TypeAwareDomainObjectIdentifier<S> identifier) {
		return create((TaskIdentifier<S>) identifier);
	}
}
