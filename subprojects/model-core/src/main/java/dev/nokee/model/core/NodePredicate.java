package dev.nokee.model.core;

/**
 * Represents a predicate scoped to a specific {@link ModelNode}.
 *
 * @param <T> the projection type matched by this predicate
 */
public interface NodePredicate<T> {
	default ModelSpec<T> scope(ModelNode node) {
		if (this instanceof ModelSpec) {
			return NodePredicateScopeStrategy.ALL.scope(node, (ModelSpec) this);
		} else {
			return (ModelSpec<T>) NodePredicateScopeStrategy.ALL.scope(node, ModelSpecs.alwaysTrue());
		}
	}
}
