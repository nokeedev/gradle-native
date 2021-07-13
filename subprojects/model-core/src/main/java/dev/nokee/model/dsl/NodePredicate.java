package dev.nokee.model.dsl;

import dev.nokee.model.core.ModelNode;
import dev.nokee.model.core.ModelSpec;
import dev.nokee.model.core.ModelSpecs;

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
