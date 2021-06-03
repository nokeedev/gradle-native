package dev.nokee.model.core;

import java.util.Optional;
import java.util.function.Predicate;

import static com.google.common.base.Predicates.alwaysTrue;
import static java.util.Objects.requireNonNull;

public abstract class NodePredicate<T> {
	private final Predicate<? super ModelNode> matcher;
	private final NodePredicateScopeStrategy scopeStrategy;

	private NodePredicate(Predicate<? super ModelNode> matcher, NodePredicateScopeStrategy scopeStrategy) {
		this.matcher = requireNonNull(matcher);
		this.scopeStrategy = scopeStrategy;
	}

	public final ModelSpec<T> scope(ModelNode node) {
		return scopeStrategy.scope(node, matcher);
	}

	abstract void doNotExtendBeyondThisPackage();

	static NodePredicate<Object> descendants() {
		return descendants(alwaysTrue());
	}

	static <T> NodePredicate<T> descendants(Predicate<? super ModelNode> predicate) {
		return new NodePredicate<T>(predicate, NodePredicateScopeStrategy.DESCENDANTS) {
			@Override
			void doNotExtendBeyondThisPackage() {}

			@Override
			public String toString() {
				return "NodePredicate.descendants(" + predicate + ")";
			}
		};
	}

	static NodePredicate<Object> directChildren() {
		return directChildren(alwaysTrue());
	}

	static <T> NodePredicate<T> directChildren(Predicate<? super ModelNode> predicate) {
		return new NodePredicate<T>(predicate, NodePredicateScopeStrategy.DIRECT_CHILDREN) {
			@Override
			void doNotExtendBeyondThisPackage() {}

			@Override
			public String toString() {
				return "NodePredicates.directChildren(" + predicate + ")";
			}
		};
	}

	private enum NodePredicateScopeStrategy {
		DESCENDANTS {
			@Override
			<T> ModelSpec<T> scope(ModelNode node, Predicate<? super ModelNode> matcher) {
				return withAncestor(node).and(matcher)::test;
			}
		},
		DIRECT_CHILDREN {
			@Override
			<T> ModelSpec<T> scope(ModelNode node, Predicate<? super ModelNode> matcher) {
				return withParent(node).and(matcher)::test;
			}
		};

		abstract <T> ModelSpec<T> scope(ModelNode node, Predicate<? super ModelNode> matcher);
	}

	private static Predicate<ModelNode> withAncestor(ModelNode node) {
		return subject -> {
			Optional<ModelNode> parent = subject.getParent();
			while (parent.isPresent()) {
				if (parent.get().equals(node)) {
					return true;
				}
				parent = parent.get().getParent();
			}
			return false;
		};
	}

	private static Predicate<ModelNode> withParent(ModelNode node) {
		return subject -> subject.getParent().map(it -> it.equals(node)).orElse(false);
	}
}
