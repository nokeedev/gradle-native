package dev.nokee.model.internal.core;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import static com.google.common.base.Predicates.alwaysTrue;
import static dev.nokee.model.internal.core.ModelNodes.withParent;

@EqualsAndHashCode
public abstract class NodePredicate {
	private final Predicate<? super ModelNode> matcher;

	private NodePredicate(Predicate<? super ModelNode> matcher) {
		this.matcher = Objects.requireNonNull(matcher);
	}

	/**
	 * Returns a model specification based on this predicate and the scoping path.
	 *
	 * @param path  the model path to scope the predicate
	 * @return a {@link ModelSpec} for matching model nodes, never null
	 */
	final ModelSpec scope(ModelPath path) {
		return scope(path, matcher);
	}

	protected abstract ModelSpec scope(ModelPath path, Predicate<? super ModelNode> matcher);

	/**
	 * Creates a predicate that matches all direct descendants of the scoped path.
	 *
	 * @return a {@link NodePredicate} matching all direct descendants, never null
	 */
	public static NodePredicate allDirectDescendants() {
		return new NodePredicate(alwaysTrue()) {
			@Override
			public ModelSpec scope(ModelPath path, Predicate<? super ModelNode> matcher) {
				return new BasicPredicateSpec(null, path, null, withParent(path).and(matcher));
			}

			@Override
			public String toString() {
				return "NodePredicate.allDirectDescendants()";
			}
		};
	}

	/**
	 * Creates a predicate that matches all direct descendants of the scoped path with the specified predicate.
	 *
	 * @param predicate  a predicate to match against all direct descendants
	 * @return a {@link NodePredicate} matching all direct descendants with a predicate, never null
	 */
	public static NodePredicate allDirectDescendants(Predicate<? super ModelNode> predicate) {
		return new NodePredicate(predicate) {
			@Override
			public ModelSpec scope(ModelPath path, Predicate<? super ModelNode> matcher) {
				return new BasicPredicateSpec(null, path, null, withParent(path).and(matcher));
			}

			@Override
			public String toString() {
				return "NodePredicate.allDirectDescendants(" + predicate + ")";
			}
		};
	}

	@ToString
	@EqualsAndHashCode
	private static final class BasicPredicateSpec implements ModelSpec {
		@Nullable
		private final ModelPath path;

		@Nullable
		private final ModelPath parent;

		@Nullable
		private final ModelPath ancestor;
		private final Predicate<? super ModelNode> matcher;
		@EqualsAndHashCode.Exclude private final Predicate<? super ModelNode> predicate;

		public BasicPredicateSpec(@Nullable ModelPath path, @Nullable ModelPath parent, @Nullable ModelPath ancestor, Predicate<? super ModelNode> matcher) {
			this.path = path;
			this.parent = parent;
			this.ancestor = ancestor;
			this.matcher = matcher;
			this.predicate = matcher;
		}

		@Override
		public boolean isSatisfiedBy(ModelNode node) {
			return predicate.test(node);
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

		@Override
		public Predicate<? super ModelNode> getMatcher() {
			return matcher;
		}
	}
}
