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
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.NamedDomainObjectProvider;

import java.util.Objects;

import static dev.nokee.model.SupportedTypes.instanceOf;
import static dev.nokee.utils.NamedDomainObjectCollectionUtils.getElementType;

final class NamedDomainObjectContainerRegistryAdapter<T> implements NamedDomainObjectRegistry<T> {
	public static <T> NamedDomainObjectContainerRegistryAdapter<T> newRegistry(NamedDomainObjectContainer<T> container) {
		return new NamedDomainObjectContainerRegistryAdapter<>(container);
	}

	private final RegistrableType registrableType;
	private final NamedDomainObjectContainer<T> container;

	private NamedDomainObjectContainerRegistryAdapter(NamedDomainObjectContainer<T> container) {
		this.container = container;
		this.registrableType = new DefaultRegistrableType(getElementType(container));
	}

	@Override
	public NamedDomainObjectProvider<T> register(String name) {
		return container.register(name);
	}

	@Override
	public NamedDomainObjectProvider<T> registerIfAbsent(String name) {
		return NamedDomainObjectCollectionUtils.registerIfAbsent(container, name);
	}

	@Override
	public RegistrableType getRegistrableType() {
		return registrableType;
	}

	@Override
	public String toString() {
		return container + " registry";
	}

	private static final class DefaultRegistrableType implements RegistrableType {
		private final SupportedType registrableType;

		private DefaultRegistrableType(Class<?> containerType) {
			this.registrableType = instanceOf(containerType);
		}

		@Override
		public boolean canRegisterType(Class<?> type) {
			Objects.requireNonNull(type);
			return registrableType.supports(type);
		}
	}
}
