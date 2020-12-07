package dev.nokee.model.internal.core;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Predicate;

import static com.google.common.base.Predicates.alwaysTrue;

public abstract class NodePredicate {
	private final Predicate<? super ModelNode> matcher;

	private NodePredicate(Predicate<? super ModelNode> matcher) {
		this.matcher = matcher;
	}

	/**
	 * Returns a model specification based on this predicate and the scoping path.
	 *
	 * @param path  the model path to scope the predicate
	 * @return a {@link ModelSpec} for matching model nodes, never null
	 */
	public ModelSpec scope(ModelPath path) {
		return scope(path, matcher);
	}

	protected abstract ModelSpec scope(ModelPath path, Predicate<? super ModelNode> predicate);

	/**
	 * Creates a predicate that matches all direct descendants of the scoped path.
	 *
	 * @return a {@link NodePredicate} matching all direct descendants, never null
	 */
	public static NodePredicate allDirectDescendants() {
		return new NodePredicate(alwaysTrue()) {
			@Override
			public ModelSpec scope(ModelPath path, Predicate<? super ModelNode> matcher) {
				return new BasicPredicateSpec(null, path, null, matcher);
			}
		};
	}

	private static final class BasicPredicateSpec implements ModelSpec {
		@Nullable
		private final ModelPath path;

		@Nullable
		private final ModelPath parent;

		@Nullable
		private final ModelPath ancestor;
		private final Predicate<? super ModelNode> matcher;

		public BasicPredicateSpec(@Nullable ModelPath path, @Nullable ModelPath parent, @Nullable ModelPath ancestor, Predicate<? super ModelNode> matcher) {
			this.path = path;
			this.parent = parent;
			this.ancestor = ancestor;
			this.matcher = matcher;
		}

		@Override
		public boolean isSatisfiedBy(ModelNode node) {
			return matcher.test(node);
		}

		@Override
		public Optional<ModelPath> getPath() {
			return Optional.ofNullable(path);
		}

		@Override
		public Optional<ModelPath> getParent() {
			return Optional.ofNullable(parent);
		}

		@Override
		public Optional<ModelPath> getAncestor() {
			return Optional.ofNullable(ancestor);
		}
	}
}
