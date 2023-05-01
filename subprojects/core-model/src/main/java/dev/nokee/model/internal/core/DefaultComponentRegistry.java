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

import com.google.common.collect.ImmutableMap;
import lombok.val;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class DefaultComponentRegistry implements ComponentRegistry {
	private final Map<Entity.Id, Map<Component.Id, Component>> components = new LinkedHashMap<>();

	@Override
	public Component set(Entity.Id entityId, Component.Id componentId, Component newComponent) {
		val idToComponents = components.computeIfAbsent(entityId, __ -> new LinkedHashMap<>());
		val oldComponent = idToComponents.get(componentId);
		idToComponents.put(componentId, newComponent);
		return oldComponent;
	}

	@Nullable
	@Override
	public Component get(Entity.Id entityId, Component.Id componentId) {
		return components.getOrDefault(entityId, ImmutableMap.of()).get(componentId);
	}

	@Override
	public Set<Component.Id> getAllIds(Entity.Id entityId) {
		return components.getOrDefault(entityId, ImmutableMap.of()).keySet();
	}

	@Override
	public Collection<Component> getAll(Entity.Id entityId) {
		return components.getOrDefault(entityId, ImmutableMap.of()).values();
	}
}
