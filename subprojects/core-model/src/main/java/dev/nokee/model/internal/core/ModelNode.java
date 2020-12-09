package dev.nokee.model.internal.core;

import com.google.common.collect.ImmutableList;
import dev.nokee.model.internal.registry.ModelConfigurer;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.model.internal.type.ModelType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * A node in the model.
 */
public final class ModelNode {
	private final ModelPath path;
	private final ModelLookup modelLookup;
	private final ModelNodeListener listener;
	private final List<ModelProjection> projections;
	private final ModelConfigurer configurer;
	private State state = State.Initialized;

	public enum State {
		Initialized,
		Registered,
		Realized
	}

	private ModelNode(ModelPath path, List<ModelProjection> projections, ModelConfigurer configurer, ModelNodeListener listener, ModelLookup modelLookup) {
		this.path = path;
		this.projections = ImmutableList.copyOf(projections);
		this.configurer = configurer;
		this.listener = listener;
		this.modelLookup = modelLookup;
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
		for (ModelProjection projection : projections) {
			if (projection.canBeViewedAs(type)) {
				return projection.get(type);
			}
		}
		throw new IllegalStateException("no projection for " + type);
	}

	public void applyTo(NodePredicate predicate, ModelAction action) {
		configurer.configureMatching(predicate.scope(getPath()), action);
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

		public ModelNode build() {
			return new ModelNode(path, projections, configurer, listener, lookup);
		}
	}
}
