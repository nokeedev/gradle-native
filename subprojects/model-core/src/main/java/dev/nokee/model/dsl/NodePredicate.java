package dev.nokee.model.dsl;

import dev.nokee.model.core.ModelNode;
import dev.nokee.model.core.ModelPredicate;
import dev.nokee.model.internal.ModelSpecs;

/**
 * Represents a predicate scoped to a specific {@link ModelNode}.
 *
 * @param <T> the projection type matched by this predicate
 */
@Deprecated
public interface NodePredicate<T> {
	default ModelSpec<T> scope(ModelNode node) {
		if (this instanceof ModelSpec) {
			return NodePredicateScopeStrategy.ALL.scope(node, (ModelSpec) this);
		} else {
			return NodePredicateScopeStrategy.ALL.scope(node, ModelSpecs.alwaysTrue().withNarrowedType());
		}
	}
}
