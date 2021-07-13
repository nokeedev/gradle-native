package dev.nokee.model.dsl;

import dev.nokee.model.core.ModelNode;
import dev.nokee.model.core.ModelProjection;
import dev.nokee.model.core.ModelSpec;
import dev.nokee.model.core.ModelSpecs;

public interface NodePredicates {
	static NodePredicate<Object> descendants() {
		return descendants(ModelSpecs.alwaysTrue());
	}

	static <T> NodePredicate<T> descendants(ModelSpec<T> spec) {
		return new AbstractNodePredicate<T>(spec, NodePredicateScopeStrategy.DESCENDANTS) {
			@Override
			public String toString() {
				return "NodePredicates.descendants(" + spec + ")";
			}
		};
	}

	static NodePredicate<Object> directChildren() {
		return directChildren(ModelSpecs.alwaysTrue());
	}

	static <T> NodePredicate<T> directChildren(ModelSpec<T> spec) {
		return new AbstractNodePredicate<T>(spec, NodePredicateScopeStrategy.DIRECT_CHILDREN) {
			@Override
			public String toString() {
				return "NodePredicates.directChildren(" + spec + ")";
			}
		};
	}

	static <T> OfTypeSpec<T> ofType(Class<T> type) {
		return new OfTypeSpec<T>() {
			private final ModelSpec<T> spec = ModelSpecs.ofType(type);

			@Override
			public boolean isSatisfiedBy(ModelProjection node) {
				return spec.isSatisfiedBy(node);
			}

			@Override
			public Class<T> getProjectionType() {
				return spec.getProjectionType();
			}

			@Override
			public ModelSpec<T> scope(ModelNode node) {
				return NodePredicateScopeStrategy.ALL.scope(node, spec);
			}
		};
	}

	interface OfTypeSpec<T> extends ModelSpec<T>, NodePredicate<T> {}
}
