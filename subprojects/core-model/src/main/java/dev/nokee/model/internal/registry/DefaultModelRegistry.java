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
package dev.nokee.model.internal.registry;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import dev.nokee.internal.reflect.Instantiator;
import dev.nokee.model.internal.core.BindManagedProjectionService;
import dev.nokee.model.internal.core.Bits;
import dev.nokee.model.internal.core.Component;
import dev.nokee.model.internal.core.ComponentRegistry;
import dev.nokee.model.internal.core.DefaultComponentRegistry;
import dev.nokee.model.internal.core.Entity;
import dev.nokee.model.internal.core.HasInputs;
import dev.nokee.model.internal.core.ModelAction;
import dev.nokee.model.internal.core.ModelComponent;
import dev.nokee.model.internal.core.ModelComponentType;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodeListener;
import dev.nokee.model.internal.core.ModelPath;
import dev.nokee.model.internal.core.ModelProjection;
import dev.nokee.model.internal.core.ModelSpec;
import dev.nokee.model.internal.core.ObservableComponentRegistry;
import lombok.val;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class DefaultModelRegistry implements ModelRegistry, ModelConfigurer, ModelLookup {
	private final Instantiator instantiator;
	private final List<ModelNode> entities = new ArrayList<>();
	private final Map<ModelPath, ModelNode> nodes = new LinkedHashMap<>();
	private final Map<Entity.Id, ModelNode> idToEntities = new LinkedHashMap<>();
	private final List<ModelAction> configurations = new ArrayList<>();
	private final NodeStateListener nodeStateListener = new NodeStateListener();
	private final BindManagedProjectionService bindingService;
	private final ModelNode rootNode;
	private final Multimap<ModelComponentType<?>, ModelAction> config = ArrayListMultimap.create();
	private final ComponentRegistry components = new ObservableComponentRegistry(new DefaultComponentRegistry(), nodeStateListener);

	public DefaultModelRegistry(Instantiator instantiator) {
		this.instantiator = instantiator;
		this.bindingService = new BindManagedProjectionService(instantiator);
		rootNode = null;
	}

	@Override
	public ModelNode get(ModelPath path) {
		Objects.requireNonNull(path);
		if (!nodes.containsKey(path)) {
			throw new IllegalArgumentException("Element at '" + path + "' wasn't found.");
		}
		return nodes.get(path);
	}

	@Override
	public Optional<ModelNode> find(ModelPath path) {
		Objects.requireNonNull(path);
		return Optional.ofNullable(nodes.get(path));
	}

	@Override
	public Result query(ModelSpec spec) {
		val result = entities.stream().filter(spec::isSatisfiedBy).collect(ImmutableList.toImmutableList());
		return new ModelLookupDefaultResult(result);
	}

	@Override
	public boolean has(ModelPath path) {
		return nodes.containsKey(Objects.requireNonNull(path));
	}

	@Override
	public boolean anyMatch(ModelSpec spec) {
		return entities.stream().anyMatch(spec::isSatisfiedBy);
	}

	@Override
	public void configure(ModelAction configuration) {
		Objects.requireNonNull(configuration);
		Preconditions.checkArgument(configuration instanceof HasInputs);
		configurations.add(configuration);
		((HasInputs) configuration).getInputs().stream().distinct().forEach(input -> {
			config.put(input.getType(), configuration);
		});

		val size = entities.size();
		for (int i = 0; i < size; i++) {
			val node = entities.get(i);
			configuration.execute(node);
		}
	}

	private final class NodeStateListener implements ModelNodeListener, ObservableComponentRegistry.Listener {
		@Override
		public void projectionAdded(ModelNode node, ModelComponent newComponent) {
			List<ModelAction> c = null;
			if (newComponent instanceof ModelProjection) {
				c = new ArrayList<>(config.get(ModelComponentType.componentOf(ModelProjection.class)));
			} else {
				c = new ArrayList<>(config.get(newComponent.getComponentType()));
			}

			Bits newComponentBits = newComponent.getComponentType().familyBits();
			c.removeIf(configuration -> !newComponentBits.intersects(((HasInputs) configuration).getInputBits()) || !node.getComponentBits().containsAll(((HasInputs) configuration).getInputBits()));

			for (int i = 0; i < c.size(); ++i) {
				val configuration = c.get(i);
				if (newComponentBits.intersects(((HasInputs) configuration).getInputBits())) { // recheck condition in case something changes
					configuration.execute(node);
				}
			}
		}

		@Override
		public void componentChanged(Entity.Id entityId, Component.Id componentId, Component component) {
			projectionAdded(idToEntities.get(entityId), (ModelComponent) component);
		}
	}
}
