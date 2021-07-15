package dev.nokee.model.dsl;

import dev.nokee.model.core.*;
import dev.nokee.model.core.ModelNode;
import dev.nokee.model.internal.ModelSpecs;

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
			private final ModelSpecs.ProjectionOfSpec<T> spec = ModelSpecs.projectionOf(type);

			@Override
			public boolean test(ModelProjection modelProjection) {
				return spec.test(modelProjection);
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
