package dev.nokee.model.internal.core;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import dev.nokee.model.DomainObjectProvider;
import dev.nokee.model.internal.registry.ModelConfigurer;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.type.ModelType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static dev.nokee.model.internal.core.ModelNodes.withPath;
import static dev.nokee.model.internal.core.NodePredicate.allDirectDescendants;

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
	private final ModelPath path;
	private final ModelLookup modelLookup;
	private final ModelNodeListener listener;
	private final List<ModelProjection> projections;
	private final ModelConfigurer configurer;
	private State state = State.Created;
	private final ModelRegistry modelRegistry;

	public enum State {
		Created,
		Initialized,
		Registered,
		Realized
	}

	private ModelNode(ModelPath path, List<ModelProjection> projections, ModelConfigurer configurer, ModelNodeListener listener, ModelLookup modelLookup, ModelRegistry modelRegistry) {
		this.path = path;
		this.projections = ImmutableList.copyOf(projections);
		this.configurer = configurer;
		this.listener = listener;
		this.modelLookup = modelLookup;
		this.modelRegistry = modelRegistry;
		listener.created(this);
		initialize();
	}

	private void initialize() {
		assert state == State.Created;
		state = State.Initialized;
		listener.initialized(this);
	}

	public ModelNode register() {
		if (!isAtLeast(State.Registered)) {
			state = State.Registered;
			listener.registered(this);
		}
		return this;
	}

	/**
	 * Realize this node.
	 *
	 * @return this model node, never null
	 */
	public ModelNode realize() {
		register();
		if (!isAtLeast(State.Realized)) {
			getParent().ifPresent(ModelNode::realize);
			state = State.Realized;
			listener.realized(this);
		}
		return this;
	}

	/**
	 * Returns the parent node of this model node, if available.
	 *
	 * @return the parent model node, never null but can be absent.
	 */
	public Optional<ModelNode> getParent() {
		return path.getParent().map(modelLookup::get);
	}

	/**
	 * Returns if the current node can be viewed as the specified type.
	 *
	 * @param type  the type to query this model node
	 * @return true if the node can be projected into the specified type, or false otherwise.
	 */
	public boolean canBeViewedAs(ModelType<?> type) {
		for (ModelProjection projection : projections) {
			if (projection.canBeViewedAs(type)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the state of this model node.
	 *
	 * @return a {@link ModelNode.State} representing the state of this model node, never null.
	 */
	public State getState() {
		return state;
	}

	/**
	 * Returns the path of this model node.
	 *
	 * @return a {@link ModelPath} representing this model node, never null.
	 */
	public ModelPath getPath() {
		return path;
	}

	/**
	 * Checks the state of the node is at or later that the specified state.
	 *
	 * @param state  the state to compare
	 * @return true if the state of the node is at or later that the specified state or false otherwise.
	 */
	public boolean isAtLeast(State state) {
		return this.state.compareTo(state) >= 0;
	}

	/**
	 * Returns the first projection matching the specified type.
	 *
	 * @param type  the type of the requested projection
	 * @param <T>  the type of the requested projection
	 * @return an instance of the projected node into the specified instance
	 * @see #get(ModelType)
	 */
	public <T> T get(Class<T> type) {
		return get(ModelType.of(type));
	}

	/**
	 * Returns the first projection matching the specified type.
	 *
	 * @param type  the type of the requested projection
	 * @param <T>  the type of the requested projection
	 * @return an instance of the projected node into the specified instance
	 */
	public <T> T get(ModelType<T> type) {
		return ModelNodeContext.of(this).execute(node -> {
			for (ModelProjection projection : projections) {
				if (projection.canBeViewedAs(type)) {
					return projection.get(type);
				}
			}
			throw new IllegalStateException("no projection for " + type);
		});
	}

	public void applyTo(NodePredicate predicate, ModelAction action) {
		configurer.configureMatching(predicate.scope(getPath()), action);
	}

	/**
	 * Returns a model provider of the relative registration.
	 *
	 * @param registration  registration request relative to this model node
	 * @param <T>  the default projection type of this registration
	 * @return a provider to the default projection of this registration.
	 */
	// TODO: Should we return a provider here?
	//  We should return a provider here so the caller can chain a configuration on the object itself when realized.
	//  We should not return a provider here, it's crossing wires in the implementation. It should return only the ModelNode
	//  On ModelRegistry, we can return a provider for NodeRegistration which would be a convenience only to bridge with the ModelRegistration API.
	public <T> DomainObjectProvider<T> register(NodeRegistration<T> registration) {
		return modelRegistry.register(registration.scope(path));
	}

	/**
	 * Returns the direct descending nodes.
	 *
	 * @return a list of directly descending nodes, never null.
	 */
	public List<ModelNode> getDirectDescendants() {
		return modelLookup.query(allDirectDescendants().scope(path)).get();
	}

	public ModelNode getDescendant(String name) {
		return modelLookup.get(path.child(name));
	}

	public boolean hasDescendant(String name) {
		return modelLookup.has(path.child(name));
	}

	/**
	 * Returns the main projection type description of this node.
	 * In practice, this describes the type of the Object projection of this node.
	 *
	 * @return the type description of this node, if present.
	 */
	// TODO: This is used to generating descriptive message. We should find a better way to do this.
	//  In the end, this fits in the reporting APIs.
	public Optional<String> getTypeDescription() {
		return Optional.ofNullable(Iterables.getFirst(projections, null))
			.map(ModelProjection::getTypeDescriptions)
			.map(it -> String.join(", ", it));
	}

	/**
	 * Apply a configuration action to this node only.
	 *
	 * @param predicate  the predicate to match before applying the action
	 * @param action  the action to execute on the node
	 */
	public void applyToSelf(Predicate<? super ModelNode> predicate, ModelAction action) {
		configurer.configureMatching(ModelSpecs.of(withPath(path).and(predicate)), action);
	}

	@Override
	public String toString() {
		return path.toString();
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
		private List<ModelProjection> projections = Collections.emptyList();
		private ModelConfigurer configurer = ModelConfigurer.failingConfigurer();
		private ModelNodeListener listener = ModelNodeListener.noOpListener();
		private ModelLookup lookup = ModelLookup.failingLookup();
		private ModelRegistry registry = failingRegistry();

		public Builder withPath(ModelPath path) {
			this.path = path;
			return this;
		}

		public Builder withProjections(ModelProjection... projections) {
			return withProjections(Arrays.asList(projections));
		}

		public Builder withProjections(List<ModelProjection> projections) {
			this.projections = projections;
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

		public ModelNode build() {
			return new ModelNode(path, projections, configurer, listener, lookup, registry);
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
