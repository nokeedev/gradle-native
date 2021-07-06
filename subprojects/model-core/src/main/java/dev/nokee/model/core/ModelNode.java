package dev.nokee.model.core;

import org.gradle.api.Named;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Represent a node in the model.
 */
public interface ModelNode extends Named {
	/**
	 * Creates a new child node for the specified identity.
	 *
	 * @param identity  the child node identity, must not be null
	 * @return a new child node, never null
	 */
	ModelNode newChildNode(Object identity);

	/**
	 * Creates a new projection attached to this node.
	 *
	 * @param builderAction  the projection builder action, must not be null
	 * @return a new projection, never null
	 */
	ModelProjection newProjection(Consumer<? super ModelProjection.Builder> builderAction);

	/**
	 * Returns the parent node of this node, if present.
	 * Root nodes does not have any parent node.
	 *
	 * @return the parent node, if present, never null
	 */
	Optional<ModelNode> getParent();

	/**
	 * Checks if the current node has a projection of the specified type attached.
	 *
	 * @param type  the projection type to test, must not be null
	 * @return {@literal true} if the current node has a projection of the specified type, or {@literal false} otherwise
	 */
	boolean canBeViewedAs(Class<?> type);
	<T> T get(Class<T> type);

	ModelNode get(Object identity);
	Optional<ModelNode> find(Object identity);
	Object getIdentity();

	Stream<ModelNode> getChildNodes();
	Stream<ModelProjection> getProjections();
}
