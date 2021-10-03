/*
 * Copyright 2020-2021 the original author or authors.
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

import dev.nokee.internal.reflect.Instantiator;
import dev.nokee.model.DomainObjectProvider;
import dev.nokee.model.internal.registry.ModelConfigurer;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.model.internal.registry.ModelRegistry;
import lombok.val;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * A node in the model.
 */
// TODO: There is 3 concept mixed in here that we will need to extract:
//  1) The model node itself, which contains path, state, projections and some convenience methods for walking the adjacent model nodes
//  2) The discovery mechanic which allows users to register other nodes to this node
//  3) The configuration mechanic which allows users to apply ModelAction to the node and descendants
//  Each of these concept should only have access to a subset of the functionality here.
//  For example, during discovery, it should not be possible to change the state of the node as well as "getting" a projection.
//  You should be able to call register(NodeRegistration) and query "what are the projection types" (but not the projection instance).
//  During configuration, it should not be allowed to call register(NodeRegistration).
//  We can disable those methods but that would be a hack.
//  Instead we should extract the concept into the proper interface and classes.
//  For now, we will move forward with the current implementation and see what will become obvious as we use the APIs.
//  NOTE: It's also important to note that when accessing ModelNode from ModelLookup, we have access to all three mechanic mixed-in.
//    We should not allow users arbitrarily accessing every mechanic of the node.
//    It should be a truly immutable node they access...
//    Maybe at the worst they can attach a configuration on the node but that can be problematic if we "finalize" the node,
//      aka prevent further changes to the node.
//    Actually, we shouldn't allow attaching configuration (applyTo, applyToSelf).
//    Instead users should go through the ModelRegistry for that and access a thin layer that gives access to the allowed query and apply methods
public final class ModelNode {
	private final ModelNodeListener listener;
	private final List<Object> components = new ArrayList<>();

	public ModelNode() {
		this.listener = ModelNodeListener.noOpListener();
	}

	private ModelNode(ModelNodeListener listener) {
		this.listener = listener;
	}

	public void addComponent(Object component) {
		components.add(component);
		listener.projectionAdded(this);
	}

	public <T> T getComponent(Class<T> type) {
		return findComponent(type).orElseThrow(RuntimeException::new);
	}

	<T> Optional<T> findComponent(Class<T> type) {
		return components.stream().filter(type::isInstance).map(type::cast).findFirst();
	}

	public boolean hasComponent(Class<?> type) {
		return components.stream().anyMatch(type::isInstance);
	}

	<T> void setComponent(Class<T> componentType, T component) {
		val existingComponent = getComponent(componentType);
		val index = components.indexOf(existingComponent);
		components.set(index, component);
	}

	public Stream<Object> getComponents() {
		return components.stream();
	}

	void notifyCreated() {
		listener.created(this);
	}

	void notifyInitialized() {
		listener.initialized(this);
	}

	void notifyRegistered() {
		listener.registered(this);
	}

	void notifyRealized() {
		listener.realized(this);
	}

	@Override
	public String toString() {
		return getComponent(ModelPath.class).toString();
	}

	/**
	 * Returns a builder for a model node.
	 *
	 * @return a builder to create a model node, never null.
	 */
	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private ModelPath path;
		private ModelConfigurer configurer = ModelConfigurer.failingConfigurer();
		private ModelNodeListener listener = ModelNodeListener.noOpListener();
		private ModelLookup lookup = ModelLookup.failingLookup();
		private ModelRegistry registry = failingRegistry();
		private Instantiator instantiator;

		public Builder withPath(ModelPath path) {
			this.path = path;
			return this;
		}

		public Builder withConfigurer(ModelConfigurer configurer) {
			this.configurer = configurer;
			return this;
		}

		public Builder withListener(ModelNodeListener listener) {
			this.listener = listener;
			return this;
		}

		public Builder withLookup(ModelLookup lookup) {
			this.lookup = lookup;
			return this;
		}

		public Builder withRegistry(ModelRegistry registry) {
			this.registry = registry;
			return this;
		}

		public Builder withInstantiator(Instantiator instantiator) {
			this.instantiator = instantiator;
			return this;
		}

		public ModelNode build() {
			val entity = new ModelNode(listener);
			entity.addComponent(path);
			entity.addComponent(new DescendantNodes(lookup, path));
			entity.addComponent(new RelativeRegistrationService(path, registry));
			entity.addComponent(new RelativeConfigurationService(path, configurer));
			entity.addComponent(new BindManagedProjectionService(instantiator));
			path.getParent().ifPresent(parentPath -> {
				entity.addComponent(new ParentNode(lookup.get(parentPath)));
			});
			ModelNodeUtils.create(entity);
			ModelNodeUtils.initialize(entity);
			return entity;
		}

		private static ModelRegistry failingRegistry() {
			return new ModelRegistry() {
				@Override
				public <T> DomainObjectProvider<T> get(ModelIdentifier<T> identifier) {
					throw new UnsupportedOperationException("This instance always fails.");
				}

				@Override
				public <T> DomainObjectProvider<T> register(NodeRegistration<T> registration) {
					throw new UnsupportedOperationException("This instance always fails.");
				}

				@Override
				public <T> DomainObjectProvider<T> register(ModelRegistration<T> registration) {
					throw new UnsupportedOperationException("This instance always fails.");
				}
			};
		}
	}
}
