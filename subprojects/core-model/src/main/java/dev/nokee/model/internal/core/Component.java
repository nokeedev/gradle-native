/*
 * Copyright 2023 the original author or authors.
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
package dev.nokee.model.internal.core;

/**
 * Represents a component in the Entity Component System (ECS).
 * Components are the data containers that store properties or attributes of an entity.
 * Implement this interface to create new types of components.
 * <p>
 * To associate a component with an entity, use a unique instance of the component's {@link Id} as a key in an entity's component storage.
 */
public interface Component {
	/**
	 * Represents the unique identifier for a specific type of component.
	 * <p>
	 * Component types can have its own implementation of this interface, creating groups of Id class for each type of component.
	 * <p>
	 * Use an instance of this class as a key to store and retrieve component instances from an entity's component storage.
	 */
	interface Id {}
}
