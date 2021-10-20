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

import dev.nokee.utils.NamedDomainObjectCollectionUtils;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.PolymorphicDomainObjectContainer;
import org.gradle.api.Task;
import org.gradle.api.internal.PolymorphicDomainObjectContainerInternal;
import org.gradle.api.tasks.TaskContainer;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

final class PolymorphicDomainObjectContainerRegistryAdapter<T> implements PolymorphicDomainObjectRegistry<T> {
	public static <T> PolymorphicDomainObjectContainerRegistryAdapter<T> newRegistry(PolymorphicDomainObjectContainer<T> container) {
		if (container instanceof TaskContainer) {
			return new PolymorphicDomainObjectContainerRegistryAdapter<>(container, new TaskContainerRegistrableTypes());
		} else {
			return new PolymorphicDomainObjectContainerRegistryAdapter<>(container, new DefaultRegistrableTypes((PolymorphicDomainObjectContainerInternal<?>) container));
		}
	}

	private final PolymorphicDomainObjectContainer<T> container;
	private final RegistrableTypes registrableTypes;

	private PolymorphicDomainObjectContainerRegistryAdapter(PolymorphicDomainObjectContainer<T> container, RegistrableTypes registrableTypes) {
		this.container = container;
		this.registrableTypes = registrableTypes;
	}

	@Override
	public <S extends T> NamedDomainObjectProvider<S> register(String name, Class<S> type) {
		Objects.requireNonNull(name);
		Objects.requireNonNull(type);
		return container.register(name, type);
	}

	@Override
	public <S extends T> NamedDomainObjectProvider<S> registerIfAbsent(String name, Class<S> type) {
		Objects.requireNonNull(name);
		Objects.requireNonNull(type);
		return NamedDomainObjectCollectionUtils.registerIfAbsent(container, name, type);
	}

	@Override
	public RegistrableTypes getRegistrableTypes() {
		return registrableTypes;
	}

	@Override
	public String toString() {
		return container + " registry";
	}

	private static final class DefaultRegistrableTypes implements RegistrableTypes {
		private final PolymorphicDomainObjectContainerInternal<?> container;

		private DefaultRegistrableTypes(PolymorphicDomainObjectContainerInternal<?> container) {
			this.container = container;
		}

		@Override
		public boolean canRegisterType(Class<?> type) {
			Objects.requireNonNull(type);
			return getSupportedTypes().anyMatch(it -> it.supports(type));
		}

		private Stream<SupportedType> getSupportedTypes() {
			return getCreatableType().stream().map(SupportedTypes::instanceOf);
		}

		private Set<? extends Class<?>> getCreatableType() {
			return container.getCreateableTypes();
		}
	}

	private static final class TaskContainerRegistrableTypes implements RegistrableTypes {
		private final SupportedType registrableTypes = SupportedTypes.anySubtypeOf(Task.class);

		@Override
		public boolean canRegisterType(Class<?> type) {
			Objects.requireNonNull(type);
			return registrableTypes.supports(type);
		}
	}
}
