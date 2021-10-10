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
import dev.nokee.model.internal.core.*;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.model.internal.state.ModelStates;
import lombok.val;

import java.util.*;

public final class DefaultModelRegistry implements ModelRegistry, ModelConfigurer, ModelLookup {
	private final Instantiator instantiator;
	private final Map<ModelPath, ModelNode> nodes = new LinkedHashMap<>();
	private final List<ModelAction> configurations = new ArrayList<>();
	private final NodeStateListener nodeStateListener = new NodeStateListener();
	private final ModelNode rootNode;

	public DefaultModelRegistry(Instantiator instantiator) {
		this.instantiator = instantiator;
		configurations.add(ModelActionWithInputs.of(ModelComponentReference.of(ModelPath.class), ModelComponentReference.of(ModelState.class), (node, path, state) -> {
			if (state.equals(ModelState.Registered)) {
				nodes.put(path, node);
			}
		}));
		rootNode = ModelStates.register(createRootNode());
	}

	private ModelNode createRootNode() {
		val path = ModelPath.root();
		val entity = new ModelNode();
		entity.addComponent(nodeStateListener);
		entity.addComponent(new DescendantNodes(this, path));
		entity.addComponent(new RelativeRegistrationService(path, this));
		entity.addComponent(new RelativeConfigurationService(path, this));
		entity.addComponent(new BindManagedProjectionService(instantiator));
		entity.addComponent(path);
		path.getParent().ifPresent(parentPath -> {
			entity.addComponent(new ParentNode(this.get(parentPath)));
		});
		return entity;
	}

	@Override
	public <T> DomainObjectProvider<T> get(ModelIdentifier<T> identifier) {
		if (!nodes.containsKey(identifier.getPath())) {
			throw new IllegalStateException(String.format("Expected model node at '%s' but none was found", identifier.getPath()));
		}

		return new ModelNodeBackedProvider<>(identifier.getType(), get(identifier.getPath()));
	}

	@Override
	public <T> DomainObjectProvider<T> register(NodeRegistration<T> registration) {
		return ModelNodeUtils.register(rootNode, registration);
	}

	@Override
	public <T> DomainObjectProvider<T> register(ModelRegistration<T> registration) {
		// TODO: Should deny creating any model registration for root node
		if (!registration.getPath().getParent().isPresent() || !nodes.containsKey(registration.getPath().getParent().get())) {
			throw new IllegalArgumentException(String.format("Model %s has to be direct descendant", registration.getPath()));
		}

		registration.getActions().forEach(configurations::add);
		val node = ModelStates.register(newNode(registration));
		return new ModelNodeBackedProvider<>(registration.getDefaultProjectionType(), node);
	}

	private ModelNode newNode(ModelRegistration<?> registration) {
		val path = registration.getPath();
		val entity = new ModelNode();
		entity.addComponent(nodeStateListener);
		entity.addComponent(new DescendantNodes(this, path));
		entity.addComponent(new RelativeRegistrationService(path, this));
		entity.addComponent(new RelativeConfigurationService(path, this));
		entity.addComponent(new BindManagedProjectionService(instantiator));
		entity.addComponent(path);
		path.getParent().ifPresent(parentPath -> {
			entity.addComponent(new ParentNode(this.get(parentPath)));
		});
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
	public Result query(ModelSpec spec) {
		val result = nodes.values().stream().filter(spec::isSatisfiedBy).collect(ImmutableList.toImmutableList());
		return new ModelLookupDefaultResult(result);
	}

	@Override
	public boolean has(ModelPath path) {
		return nodes.containsKey(Objects.requireNonNull(path));
	}

	@Override
	public boolean anyMatch(ModelSpec spec) {
		return nodes.values().stream().anyMatch(spec::isSatisfiedBy);
	}

	@Override
	public void configure(ModelAction configuration) {
		configurations.add(configuration);
		val size = nodes.size();
		for (int i = 0; i < size; i++) {
			val node = Iterables.get(nodes.values(), i);
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
