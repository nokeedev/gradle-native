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

import org.gradle.api.InvalidUserDataException;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.PolymorphicDomainObjectContainer;
import org.gradle.api.provider.Provider;

public interface PolymorphicDomainObjectRegistry<T> {
	/**
	 * Creates a registry for the specified container.
	 *
	 * @param container  a named domain object container, must not be null
	 * @param <T>  the type of objects to register
	 * @return a registry adapter for the specified container, never null
	 */
	static <T> PolymorphicDomainObjectRegistry<T> of(PolymorphicDomainObjectContainer<T> container) {
		return PolymorphicDomainObjectContainerRegistryAdapter.newRegistry(container);
	}

	/**
	 * Defines a new object, which will be created when it is required.
	 * An object is 'required' when the object is located using query methods or when {@link Provider#get()} is called on the return value of this method.
	 *
	 * @param name  the name of the object, must not be null
	 * @param type  the object type, must not be null
	 * @return a {@link Provider} that whose value will be the object, when queried, never null
	 * @throws InvalidUserDataException If a object with the given name already exists in this project.
	 */
	<S extends T> NamedDomainObjectProvider<S> register(String name, Class<S> type) throws InvalidUserDataException;

	/**
	 * Defines a new object, which will be created when it is required, only if an object with the same name does not already exist.
	 * An object is 'required' when the object is located using query methods or when {@link Provider#get()} is called on the return value of this method.
	 * If the object already exist, the same provider will be returned.
	 *
	 * @param name  the name of the object, must not be null
	 * @param type  the object type, must not be null
	 * @return a {@link Provider} that whose value will be the object, when queried, never null
	 */
	<S extends T> NamedDomainObjectProvider<S> registerIfAbsent(String name, Class<S> type);

	/**
	 * Returns the registrable types known to this registry.
	 *
	 * @return registrable types of this registry, never null
	 */
	RegistrableTypes getRegistrableTypes();

	/**
	 * Represent all registrable types for the registry.
	 */
	interface RegistrableTypes {
		/**
		 * Returns {@literal true} if the specified type is registrable.
		 *
		 * @param type  the type to check against registry, must not be null
		 * @return {@code true} if the specified type is registrable or {@code false} otherwise
		 */
		boolean canRegisterType(Class<?> type);
	}
}
