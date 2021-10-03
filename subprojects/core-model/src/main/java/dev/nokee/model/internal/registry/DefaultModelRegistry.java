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
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.model.internal.type.ModelType;
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
		rootNode = ModelStates.register(createRootNode());
		nodes.put(ModelPath.root(), rootNode);
	}

	private ModelNode createRootNode() {
		return ModelNode.builder()
			.withPath(ModelPath.root())
			.withConfigurer(this)
			.withLookup(this)
			.withListener(nodeStateListener)
			.withRegistry(this)
			.withInstantiator(instantiator)
			.build();
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
			throw new IllegalArgumentException("Has to be direct descendant");
		}

		registration.getActions().forEach(configurations::add);
		val node = ModelStates.register(newNode(registration));
		return new ModelNodeBackedProvider<>(registration.getDefaultProjectionType(), node);
	}

	private ModelNode newNode(ModelRegistration<?> registration) {
		return ModelNode.builder()
			.withPath(registration.getPath())
			.withConfigurer(this)
			.withListener(nodeStateListener)
			.withLookup(this)
			.withRegistry(this)
			.withInstantiator(instantiator)
			.build();
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
			configuration.execute(Iterables.get(nodes.values(), i));
		}
	}

	private final class NodeStateListener implements ModelNodeListener {
		@Override
		public void created(ModelNode node) {
			notify(node);
		}

		@Override
		public void initialized(ModelNode node) {
			notify(node);
		}

		@Override
		public void registered(ModelNode node) {
			nodes.put(ModelNodeUtils.getPath(node), node);
			notify(node);
		}

		@Override
		public void realized(ModelNode node) {
			notify(node);
		}

		@Override
		public void projectionAdded(ModelNode node, Object newComponent) {
			// Splitting the execution until we reconcile ModelAction with the concept of System and Observer.
			for (int i = 0; i < configurations.size(); ++i) {
				val configuration = configurations.get(i);
				if (configuration instanceof ModelActionWithInputs) {
					if (((ModelActionWithInputs) configuration).getInputs().contains(ModelType.typeOf(newComponent))) {
						configuration.execute(node);
					}
				}
			}
		}

		private void notify(ModelNode node) {
			for (int i = 0; i < configurations.size(); ++i) {
				val configuration = configurations.get(i);
				if (!(configuration instanceof ModelActionWithInputs)) {
					configuration.execute(node);
				}
			}
		}
	}
}
