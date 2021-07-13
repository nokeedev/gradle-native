package dev.nokee.model.dsl;

import dev.nokee.model.core.ModelNode;
import dev.nokee.model.core.ModelSpec;

import static dev.nokee.model.core.ModelSpecs.*;

enum NodePredicateScopeStrategy {
	ALL {
		@Override
		<T> ModelSpec<T> scope(dev.nokee.model.core.ModelNode node, ModelSpec<T> matcher) {
			return isSelf(node).or(withAncestor(node)).and(matcher);
		}
	},
	DESCENDANTS {
		@Override
		<T> ModelSpec<T> scope(dev.nokee.model.core.ModelNode node, ModelSpec<T> matcher) {
			return withAncestor(node).and(matcher);
		}
	},
	DIRECT_CHILDREN {
		@Override
		<T> ModelSpec<T> scope(dev.nokee.model.core.ModelNode node, ModelSpec<T> matcher) {
			return withParent(node).and(matcher);
		}
	};

	abstract <T> ModelSpec<T> scope(ModelNode node, ModelSpec<T> matcher);
}
