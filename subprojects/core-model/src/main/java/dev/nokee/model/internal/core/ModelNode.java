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
import dev.nokee.model.internal.names.ElementNameComponent;
import dev.nokee.model.internal.registry.ModelConfigurer;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelStates;
import lombok.val;
import org.apache.commons.lang3.mutable.MutableObject;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
	private final ModelEntityId id = ModelEntityId.nextId();
	private final ModelComponentRegistry components;

	// Represent all components this entity has.
	private Bits componentBits = Bits.empty();

	public ModelNode() {
		components = new DefaultComponentRegistry();
	}

	public ModelNode(ModelComponentRegistry components) {
		this.components = components;
	}

	public ModelEntityId getId() {
		return id;
	}

	public Bits getComponentBits() {
		return componentBits;
	}

	public <T extends ModelComponent> T addComponent(T component) {
		@SuppressWarnings("unchecked")
		final ModelComponentType<T> componentType = (ModelComponentType<T>) component.getComponentType();
		val oldComponent = components.get(id, componentType);
		if (oldComponent == null || !oldComponent.equals(component)) {
			if (oldComponent == null) {
				componentBits = componentBits.or(componentType.familyBits());
			}
			components.set(id, componentType, component);
		}
		return component;
	}

	public <T extends ModelProjection> T addComponent(T component) {
		@SuppressWarnings("unchecked")
		final ModelComponentType<T> componentType = (ModelComponentType<T>) component.getComponentType();
		val oldComponent = components.get(id, componentType);
		if (oldComponent == null) {
			componentBits = componentBits.or(componentType.familyBits());
			components.set(id, componentType, component);
		}
		return component;
	}

	public ModelComponentTypes getComponentTypes() {
		@SuppressWarnings("unchecked")
		final Set<ModelComponentType<?>> allIds = (Set<ModelComponentType<?>>) components.getAllIds(id);
		return new ModelComponentTypes(allIds);
	}

	public <T extends ModelComponent> T get(Class<T> type) {
		return getComponent(ModelComponentType.componentOf(type));
	}

	public <T extends ModelComponent> T getComponent(ModelComponentType<T> componentType) {
		return findComponent(componentType).orElseThrow(() -> {
			return new RuntimeException(String.format("No components of type '%s'. Available: %s", componentType, components.getAllIds(id).stream().map(Objects::toString).collect(Collectors.joining(", "))));
		});
	}

	public <T extends ModelComponent> Optional<T> find(Class<T> type) {
		return findComponent(ModelComponentType.componentOf(type));
	}

	public <T extends ModelComponent> Optional<T> findComponent(ModelComponentType<T> componentType) {
		@SuppressWarnings("unchecked")
		final T result = (T) components.get(id, componentType);
		return Optional.ofNullable(result);
	}

	@Nullable
	public <T extends ModelComponent> T findComponentNullable(ModelComponentType<T> componentType) {
		@SuppressWarnings("unchecked")
		final T result = (T) components.get(id, componentType);
		return result;
	}

	public boolean hasComponent(ModelComponentType<? extends ModelComponent> componentType) {
		return components.get(id, componentType) != null;
	}

	public <T extends ModelComponent> boolean has(Class<T> type) {
		return hasComponent(ModelComponentType.componentOf(type));
	}

	public <T extends ModelComponent> void setComponent(Class<T> componentType, T component) {
		setComponent(component);
	}

	public <T extends ModelComponent> void setComponent(T component) {
		@SuppressWarnings("unchecked")
		val componentType = (ModelComponentType<T>) component.getComponentType();
		if (components.get(id, componentType) == null) {
			throw new RuntimeException();
		}
		componentBits = components.getAllIds(id).stream().map(it -> (ModelComponentType<?>) it)
			.map(ModelComponentType::familyBits).reduce(Bits.empty(), Bits::or);
		components.set(id, componentType, component);
	}

	public Stream<ModelComponent> getComponents() {
		return components.getAll(id).stream();
	}

	@Override
	public String toString() {
		return find(ModelPathComponent.class).map(ModelPathComponent::get).map(Objects::toString).orElseGet(() -> "entity '" + id + "'");
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
			MutableObject<ModelNode> self = new MutableObject<>();
			val entity = new ModelNode(new ObservableComponentRegistry(new DefaultComponentRegistry(), new ObservableComponentRegistry.Listener() {
				@Override
				public void componentChanged(ModelEntityId entityId, Component.Id componentId, ModelComponent component) {
					listener.projectionAdded(self.getValue(), component);
				}
			}));
			self.setValue(entity);
			entity.addComponent(new DescendantNodes(lookup, path));
			entity.addComponent(new RelativeRegistrationService(registry));
			entity.addComponent(new BindManagedProjectionService(instantiator));
			entity.addComponent(new ModelPathComponent(path));
			path.getParent().ifPresent(parentPath -> {
				entity.addComponent(new ParentComponent(lookup.get(parentPath)));
			});
			entity.addComponent(new ElementNameComponent(path.getName()));
			entity.addComponent(new DisplayNameComponent(path.toString()));
			ModelStates.create(entity);
			ModelStates.initialize(entity);
			return entity;
		}

		private static ModelRegistry failingRegistry() {
			return new ModelRegistry() {
				@Override
				public <T> DomainObjectProvider<T> get(ModelIdentifier<T> identifier) {
					throw new UnsupportedOperationException("This instance always fails.");
				}

				@Override
				public ModelNode instantiate(ModelRegistration registration) {
					throw new UnsupportedOperationException("This instance always fails.");
				}

				@Override
				public ModelElement register(ModelRegistration registration) {
					throw new UnsupportedOperationException("This instance always fails.");
				}
			};
		}
	}
}
