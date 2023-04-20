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

import lombok.val;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Set;

public final class ObservableComponentRegistry implements ModelComponentRegistry {
	private final ModelComponentRegistry delegate;
	private final Listener listener;

	public ObservableComponentRegistry(ModelComponentRegistry delegate, Listener listener) {
		this.delegate = delegate;
		this.listener = listener;
	}

	@Nullable
	@Override
	public ModelComponent set(ModelEntityId entityId, ModelComponentId componentId, ModelComponent newComponent) {
		val oldComponent = delegate.set(entityId, componentId, newComponent);
		if (oldComponent == null || !oldComponent.equals(newComponent)) {
			listener.componentChanged(entityId, componentId, newComponent);
		}
		return oldComponent;
	}

	@Nullable
	@Override
	public ModelComponent get(ModelEntityId entityId, ModelComponentId componentId) {
		return delegate.get(entityId, componentId);
	}

	@Override
	public Set<? extends ModelComponentId> getAllIds(ModelEntityId entityId) {
		return delegate.getAllIds(entityId);
	}

	@Override
	public Collection<ModelComponent> getAll(ModelEntityId entityId) {
		return delegate.getAll(entityId);
	}

	public interface Listener {
		void componentChanged(ModelEntityId entityId, ModelComponentId componentId, ModelComponent component);
	}
}
