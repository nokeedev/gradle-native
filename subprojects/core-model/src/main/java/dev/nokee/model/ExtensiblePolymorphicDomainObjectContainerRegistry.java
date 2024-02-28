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
import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer;
import org.gradle.api.NamedDomainObjectFactory;
import org.gradle.api.NamedDomainObjectProvider;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

public final class ExtensiblePolymorphicDomainObjectContainerRegistry<ElementType> implements PolymorphicDomainObjectRegistry<ElementType> {
	private final Set<Class<? extends ElementType>> creatableTypes = new LinkedHashSet<>();
	private final ExtensiblePolymorphicDomainObjectContainer<ElementType> container;
	private final RegistrableTypes registrableTypes = new DefaultRegistrableTypes();

	public ExtensiblePolymorphicDomainObjectContainerRegistry(ExtensiblePolymorphicDomainObjectContainer<ElementType> container) {
		this.container = container;
	}

	@Override
	public <S extends ElementType> NamedDomainObjectProvider<S> register(String name, Class<S> type) {
		Objects.requireNonNull(name);
		Objects.requireNonNull(type);
		return container.register(name, type);
	}

	@Override
	public <S extends ElementType> NamedDomainObjectProvider<S> registerIfAbsent(String name, Class<S> type) {
		Objects.requireNonNull(name);
		Objects.requireNonNull(type);
		return NamedDomainObjectCollectionUtils.registerIfAbsent(container, name, type);
	}

	public <U extends ElementType> void registerFactory(Class<U> type, NamedDomainObjectFactory<? extends U> factory) {
		container.registerFactory(type, factory);
		creatableTypes.add(type);
	}

	@Override
	public RegistrableTypes getRegistrableTypes() {
		return registrableTypes;
	}

	@Override
	public String toString() {
		return container + " registry";
	}

	private final class DefaultRegistrableTypes implements RegistrableTypes {
		@Override
		public boolean canRegisterType(Class<?> type) {
			Objects.requireNonNull(type);
			return getSupportedTypes().anyMatch(it -> it.supports(type));
		}

		private Stream<SupportedType> getSupportedTypes() {
			return getCreatableType().stream().map(SupportedTypes::instanceOf);
		}

		private Set<? extends Class<?>> getCreatableType() {
			return creatableTypes;
		}
	}
}
