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
 * Represents an entity in the Entity Component System (ECS).
 * Entities are lightweight objects that serve as contains for components.
 * <p>
 * Entities are identified by their unique {@link Id} instances, which can be used to access and manage their associated components.
 * <p>
 * Implement this interface to create convenient types to work with entities of the ECS.
 */
public interface Entity {
	/**
	 * Returns the unique identifier for this entity.
	 *
	 * @return the {@link Id} instance representing the unique identifier for this entity
	 */
	Id getId();

	/**
	 * Represents the unique identifier for a specific entity.
	 * <p>
	 * Use an instance of this class to identify and manage entities within the ECS.
	 */
	interface Id {}
}
