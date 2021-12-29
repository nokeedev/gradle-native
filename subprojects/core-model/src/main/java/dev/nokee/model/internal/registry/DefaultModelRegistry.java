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
import com.google.common.collect.Iterables;
import dev.nokee.internal.reflect.Instantiator;
import dev.nokee.model.DomainObjectProvider;
import dev.nokee.model.internal.DefaultModelObject;
import dev.nokee.model.internal.core.*;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.model.internal.state.ModelStates;
import lombok.val;

import java.util.*;

public final class DefaultModelRegistry implements ModelRegistry, ModelConfigurer, ModelLookup {
	private final Instantiator instantiator;
	private final List<ModelNode> entities = new ArrayList<>();
	private final Map<ModelPath, ModelNode> nodes = new LinkedHashMap<>();
	private final List<ModelAction> configurations = new ArrayList<>();
	private final NodeStateListener nodeStateListener = new NodeStateListener();
	private final BindManagedProjectionService bindingService;
	private final ModelNode rootNode;

	public DefaultModelRegistry(Instantiator instantiator) {
		this.instantiator = instantiator;
		this.bindingService = new BindManagedProjectionService(instantiator);
		configurations.add(ModelActionWithInputs.of(ModelComponentReference.of(ModelPath.class), ModelComponentReference.of(ModelState.class), (node, path, state) -> {
			if (state.equals(ModelState.Created)) {
				if (!path.equals(ModelPath.root()) && (!path.getParent().isPresent() || !nodes.containsKey(path.getParent().get()))) {
					throw new IllegalArgumentException(String.format("Model %s has to be direct descendant", path));
				}

				node.addComponent(new DescendantNodes(this, path));
				node.addComponent(new RelativeRegistrationService(path, this));
				node.addComponent(new RelativeConfigurationService(path, this));
				node.addComponent(new BindManagedProjectionService(instantiator));
				path.getParent().ifPresent(parentPath -> {
					node.addComponent(new ParentNode(this.get(parentPath)));
				});
				node.addComponent(new ElementNameComponent(path.getName()));
				node.addComponent(new DisplayNameComponent(path.toString()));
			} else if (state.equals(ModelState.Registered)) {
				assert !entities.contains(node) : "duplicated registered notification";
				nodes.put(path, node);
				entities.add(node);
			}
		}));
		rootNode = ModelStates.register(createRootNode());
	}

	private ModelNode createRootNode() {
		val path = ModelPath.root();
		val entity = new ModelNode();
		entity.addComponent(nodeStateListener);
		entity.addComponent(path);
		return entity;
	}

	@Override
	public <T> DomainObjectProvider<T> get(ModelIdentifier<T> identifier) {
		if (!nodes.containsKey(identifier.getPath())) {
			throw new IllegalStateException(String.format("Expected model node at '%s' but none was found", identifier.getPath()));
		}

		return DefaultModelObject.of(identifier.getType(), get(identifier.getPath()));
	}

	@Override
	public ModelElement register(NodeRegistration registration) {
		return ModelNodeUtils.register(rootNode, registration);
	}

	@Override
	public ModelElement register(ModelRegistration registration) {
		registration.getActions().forEach(this::configure);
		val node = ModelStates.register(newNode(registration));
		return DefaultModelElement.of(node);
	}

	private ModelNode newNode(ModelRegistration registration) {
		val entity = new ModelNode();
		entity.addComponent(nodeStateListener);
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
		configurations.add(configuration);
		val size = entities.size();
		for (int i = 0; i < size; i++) {
			val node = entities.get(i);
			if (configuration instanceof HasInputs && ((HasInputs) configuration).getInputs().stream().allMatch(it -> ((ModelComponentReferenceInternal) it).isSatisfiedBy(node.getComponentTypes()))) {
				configuration.execute(node);
			} else {
				configuration.execute(node);
			}
		}
	}

	private final class NodeStateListener implements ModelNodeListener {
		@Override
		public void projectionAdded(ModelNode node, Object newComponent) {
			for (int i = 0; i < configurations.size(); ++i) {
				val configuration = configurations.get(i);
				if (configuration instanceof HasInputs) {
					if (((HasInputs) configuration).getInputs().stream().anyMatch(it -> ((ModelComponentReferenceInternal) it).isSatisfiedBy(ModelComponentType.ofInstance(newComponent))) && ((HasInputs) configuration).getInputs().stream().allMatch(it -> ((ModelComponentReferenceInternal) it).isSatisfiedBy(node.getComponentTypes()))) {
						configuration.execute(node);
					} else if (((HasInputs) configuration).getInputs().isEmpty()) {
						configuration.execute(node);
					}
				} else {
					configuration.execute(node);
				}
			}
		}
	}
}
