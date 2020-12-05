package dev.nokee.model.internal.core;

import dev.nokee.model.internal.type.ModelType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A node in the model.
 */
public final class ModelNode {
	private final ModelPath path;
	private final List<ModelProjection> projections = new ArrayList<>();

	public ModelNode(ModelPath path) {
		this(path, Collections.emptyList());
	}

	public ModelNode(ModelPath path, List<ModelProjection> projections) {
		this.path = path;
		this.projections.addAll(projections);
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
}
