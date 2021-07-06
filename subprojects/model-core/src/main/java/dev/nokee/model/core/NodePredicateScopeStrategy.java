package dev.nokee.model.core;

import static dev.nokee.model.core.ModelSpecs.*;

public enum NodePredicateScopeStrategy {
	ALL {
		@Override
		<T> ModelSpec<T> scope(ModelNode node, ModelSpec<T> matcher) {
			return isSelf(node).or(withAncestor(node)).and(matcher);
		}
	},
	DESCENDANTS {
		@Override
		<T> ModelSpec<T> scope(ModelNode node, ModelSpec<T> matcher) {
			return withAncestor(node).and(matcher);
		}
	},
	DIRECT_CHILDREN {
		@Override
		<T> ModelSpec<T> scope(ModelNode node, ModelSpec<T> matcher) {
			return withParent(node).and(matcher);
		}
	};

	abstract <T> ModelSpec<T> scope(ModelNode node, ModelSpec<T> matcher);
}
