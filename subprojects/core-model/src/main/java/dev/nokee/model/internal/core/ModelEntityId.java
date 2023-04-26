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
package dev.nokee.model.internal.core;

import lombok.EqualsAndHashCode;

/**
 * Represent an entity at its simplest form.
 */
@EqualsAndHashCode
public final class ModelEntityId implements Entity.Id {
	private static long nextId = 0;
	private final long id;

	private ModelEntityId(long id) {
		this.id = id;
	}

	/**
	 * Creates an entity id of the specified value.
	 * Only use this factory method when the entity id is already allocated.
	 * Use {@link #nextId()} to return the next available entity id.
	 *
	 * @param id  a known entity id, should be positive
	 * @return an entity id of the specified value, never null
	 */
	public static ModelEntityId ofId(long id) {
		return new ModelEntityId(id);
	}

	/**
	 * Returns the next available entity id.
	 *
	 * @return an entity id, never null
	 */
	public synchronized static ModelEntityId nextId() {
		return new ModelEntityId(nextId++);
	}

	@Override
	public String toString() {
		return String.valueOf(id);
	}
}
