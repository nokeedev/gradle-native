package dev.nokee.model.internal.core;

import dev.nokee.model.internal.registry.ModelConfigurer;
import dev.nokee.model.internal.type.ModelType;

import java.util.ArrayList;
import java.util.List;

/**
 * A node in the model.
 */
public final class ModelNode {
	private final ModelPath path;
	private final ModelNodeListener listener;
	private final List<ModelProjection> projections = new ArrayList<>();
	private final ModelConfigurer configurer;
	private State state = State.Initialized;

	public enum State {
		Initialized,
		Registered
	}

	public ModelNode(ModelPath path, List<ModelProjection> projections) {
		this(path, projections, ModelConfigurer.failingConfigurer(), ModelNodeListener.noOpListener());
	}

	public ModelNode(ModelPath path, List<ModelProjection> projections, ModelConfigurer configurer, ModelNodeListener listener) {
		this.path = path;
		this.projections.addAll(projections);
		this.configurer = configurer;
		this.listener = listener;
		listener.initialized(this);
	}

	public ModelNode register() {
		if (!isAtLeast(State.Registered)) {
			state = State.Registered;
			listener.registered(this);
		}
		return this;
	}

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
		return null; // TODO: throw exception
	}

	public void applyTo(NodePredicate predicate, ModelAction action) {
		configurer.configureMatching(predicate.scope(getPath()), action);
	}
}
