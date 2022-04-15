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

import com.google.common.collect.ImmutableList;
import dev.nokee.internal.reflect.Instantiator;
import dev.nokee.model.DomainObjectProvider;
import dev.nokee.model.internal.ModelElementFactory;
import dev.nokee.model.internal.core.BindManagedProjectionService;
import dev.nokee.model.internal.core.Bits;
import dev.nokee.model.internal.core.DescendantNodes;
import dev.nokee.model.internal.core.DisplayNameComponent;
import dev.nokee.model.internal.core.HasInputs;
import dev.nokee.model.internal.core.ModelAction;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelComponentReference;
import dev.nokee.model.internal.core.ModelComponentType;
import dev.nokee.model.internal.core.ModelElement;
import dev.nokee.model.internal.core.ModelIdentifier;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodeListener;
import dev.nokee.model.internal.core.ModelNodeListenerComponent;
import dev.nokee.model.internal.core.ModelNodeUtils;
import dev.nokee.model.internal.core.ModelPath;
import dev.nokee.model.internal.core.ModelPathComponent;
import dev.nokee.model.internal.core.ModelProjection;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.core.ModelSpec;
import dev.nokee.model.internal.core.NodeRegistration;
import dev.nokee.model.internal.core.ParentComponent;
import dev.nokee.model.internal.core.RelativeConfigurationService;
import dev.nokee.model.internal.core.RelativeRegistrationService;
import dev.nokee.model.internal.names.ElementNameComponent;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.model.internal.state.ModelStates;
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
	private final List<ModelAction> configurations = new ArrayList<>();
	private final NodeStateListener nodeStateListener = new NodeStateListener();
	private final BindManagedProjectionService bindingService;
	private final ModelNode rootNode;
	private final ModelElementFactory elementFactory;

	public DefaultModelRegistry(Instantiator instantiator) {
		this.instantiator = instantiator;
		this.elementFactory = new ModelElementFactory(instantiator);
		this.bindingService = new BindManagedProjectionService(instantiator);
		configurations.add(ModelActionWithInputs.of(ModelComponentReference.of(ModelPathComponent.class), ModelComponentReference.of(ModelState.class), (node, path, state) -> {
			if (state.equals(ModelState.Created)) {
				if (!path.get().equals(ModelPath.root()) && (!path.get().getParent().isPresent() || !nodes.containsKey(path.get().getParent().get()))) {
					throw new IllegalArgumentException(String.format("Model %s has to be direct descendant", path));
				}

				node.addComponent(new DescendantNodes(this, path.get()));
				node.addComponent(new RelativeRegistrationService(path.get(), this));
				node.addComponent(new RelativeConfigurationService(path.get(), this));
				node.addComponent(new BindManagedProjectionService(instantiator));
				node.addComponent(new ElementNameComponent(path.get().getName()));
				path.get().getParent().ifPresent(parentPath -> {
					node.addComponent(new ParentComponent(this.get(parentPath)));
				});
				node.addComponent(new ElementNameComponent(path.get().getName()));
				if (!node.has(DisplayNameComponent.class)) {
					node.addComponent(new DisplayNameComponent(path.get().toString()));
				}
			} else if (state.equals(ModelState.Registered)) {
				assert !nodes.values().contains(node) : "duplicated registered notification";
				nodes.put(path.get(), node);
			}
		}));
		rootNode = ModelStates.register(createRootNode());
	}

	private ModelNode createRootNode() {
		val path = ModelPath.root();
		val entity = new ModelNode();
		entities.add(entity);
		entity.addComponent(new ModelNodeListenerComponent(nodeStateListener));
		entity.addComponent(new ModelPathComponent(path));
		return entity;
	}

	@Override
	public <T> DomainObjectProvider<T> get(ModelIdentifier<T> identifier) {
		if (!nodes.containsKey(identifier.getPath())) {
			throw new IllegalStateException(String.format("Expected model node at '%s' but none was found", identifier.getPath()));
		}

		return elementFactory.createObject(get(identifier.getPath()), identifier.getType());
	}

	@Override
	public ModelNode instantiate(ModelRegistration registration) {
		registration.getActions().forEach(this::configure);
		val node = new ModelNode(nodeStateListener);
		entities.add(node);
		return newNode(node, registration);
	}

	@Override
	public ModelElement register(NodeRegistration registration) {
		return ModelNodeUtils.register(rootNode, registration);
	}

	@Override
	public ModelElement register(ModelRegistration registration) {
		return elementFactory.createElement(ModelStates.register(instantiate(registration)));
	}

	private ModelNode newNode(ModelNode entity, ModelRegistration registration) {
		entity.addComponent(elementFactory);
		for (Object component : registration.getComponents()) {
			if (component instanceof ModelProjection) {
				entity.addComponent(bindingService.bindManagedProjectionWithInstantiator((ModelProjection) component));
			} else {
				entity.addComponent(component);
			}
		}
		return entity;
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
		configurations.add(configuration);
		val size = entities.size();
		for (int i = 0; i < size; i++) {
			val node = entities.get(i);
			configuration.execute(node);
		}
	}

	private final class NodeStateListener implements ModelNodeListener {
		@Override
		public void projectionAdded(ModelNode node, Object newComponent) {
			Bits newComponentBits = ModelComponentType.ofInstance(newComponent).familyBits();
			for (int i = 0; i < configurations.size(); ++i) {
				val configuration = configurations.get(i);
				if (configuration instanceof HasInputs) {
					if (newComponentBits.intersects(((HasInputs) configuration).getInputBits())) {
						configuration.execute(node);
					}
				} else {
					configuration.execute(node);
				}
			}
		}
	}
}
