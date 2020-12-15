package dev.nokee.model.internal.core;

import dev.nokee.model.internal.type.ModelType;
import lombok.EqualsAndHashCode;
import lombok.val;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.specs.Spec;

import java.util.function.Predicate;

/**
 * A group of common model node operations.
 */
public final class ModelNodes {
	private ModelNodes() {}

	/**
	 * Returns a predicate that evaluates to true if both of its components evaluate to true.
	 * The components are evaluated in order, and evaluation will be "short-circuited" as soon as a false predicate is found.
	 *
	 * @param first  the first predicate to evaluate
	 * @param second  the second predicate to evaluate
	 * @return a predicate evaluating to true only if both predicates evaluates to true.
	 */
	public static Predicate<ModelNode> and(Predicate<? super ModelNode> first, Predicate<? super ModelNode> second) {
		return new AndPredicate(first, second);
	}

	@EqualsAndHashCode
	private static final class AndPredicate implements Predicate<ModelNode> {
		private final Predicate<? super ModelNode> first;
		private final Predicate<? super ModelNode> second;

		public AndPredicate(Predicate<? super ModelNode> first, Predicate<? super ModelNode> second) {
			this.first = first;
			this.second = second;
		}

		@Override
		public boolean test(ModelNode node) {
			return first.test(node) && second.test(node);
		}

		@Override
		public String toString() {
			return "ModelNodes.and(" + first + ", " + second + ")";
		}
	}

	// TODO: All custom predicate here should extends from this base predicate
	private static abstract class AbstractModelNodePredicate implements Predicate<ModelNode> {
		// TODO: We should test this method for all custom predicate
		@Override
		public Predicate<ModelNode> and(Predicate<? super ModelNode> other) {
			return new AndPredicate(this, other);
		}

		// TODO: Support or
		// TODO: Support negate
	}

	/**
	 * Returns the decorated model node of the specified instance.
	 * It is safe to say that for managed model node projection instance, a model node will be present.
	 *
	 * @param target  the instance to cross-reference with it's model node
	 * @return the model node for the specified instance if available
	 */
	public static ModelNode of(Object target) {
		if (target instanceof ModelNodeAware) {
			return ((ModelNodeAware) target).getNode();
		} else if (target instanceof ExtensionAware) {
			val node = ((ExtensionAware) target).getExtensions().findByType(ModelNode.class);
			if (node == null) {
				throw objectNotDecoratedWithModelNode(target);
			}
			return node;
		}
		throw objectNotDecoratedWithModelNode(target);
	}

	public static <T> T inject(T target, ModelNode node) {
		assert target instanceof ExtensionAware;
		((ExtensionAware) target).getExtensions().add(ModelNode.class, "__NOKEE_modelNode", node);
		return target;
	}

	/**
	 * Returns a predicate filtering model nodes by the specified type.
	 *
	 * @param type  the type to filter model node
	 * @return a predicate matching model nodes by type, never null.
	 */
	// TODO: Rename to subtypeOf
	public static Predicate<ModelNode> withType(ModelType<?> type) {
		return new WithTypePredicate(type);
	}

	@EqualsAndHashCode
	private static final class WithTypePredicate implements Predicate<ModelNode> {
		private final ModelType<?> type;

		private WithTypePredicate(ModelType<?> type) {
			this.type = type;
		}

		@Override
		public boolean test(ModelNode node) {
			return node.canBeViewedAs(type);
		}

		@Override
		public String toString() {
			return "ModelNodes.withType(" + type + ")";
		}
	}

	/**
	 * Returns a predicate filtering model nodes by the specified state.
	 *
	 * @param state  the state to filter model node
	 * @return a predicate matching model node by state, never null.
	 */
	public static Predicate<ModelNode> stateAtLeast(ModelNode.State state) {
		return new StateAtLeastPredicate(state);
	}

	@EqualsAndHashCode
	private static final class StateAtLeastPredicate implements Predicate<ModelNode> {
		private final ModelNode.State state;

		private StateAtLeastPredicate(ModelNode.State state) {
			this.state = state;
		}

		@Override
		public boolean test(ModelNode node) {
			return node.isAtLeast(state);
		}

		@Override
		public String toString() {
			return "ModelNodes.stateAtLeast(" + state + ")";
		}
	}

	/**
	 * Returns a predicate filtering model nodes that satisfy the specified spec for the projection type specified.
	 *
	 * @param type  the projection type to match using the spec
	 * @param spec  the spec to satisfy using a projection of the model node
	 * @param <T> the projection type
	 * @return a predicate matching model nodes by spec of a projection, never null.
	 */
	public static <T> Predicate<ModelNode> isSatisfiedByProjection(ModelType<T> type, Spec<? super T> spec) {
		return new SatisfiedByProjectionSpecAdapter<>(type, spec);
	}

	private static final class SatisfiedByProjectionSpecAdapter<T> implements Predicate<ModelNode> {
		private final ModelType<T> type;
		private final Spec<? super T> spec;

		private SatisfiedByProjectionSpecAdapter(ModelType<T> type, Spec<? super T> spec) {
			this.type = type;
			this.spec = spec;
		}

		@Override
		public boolean test(ModelNode node) {
			return node.canBeViewedAs(type) && spec.isSatisfiedBy(node.get(type));
		}

		@Override
		public String toString() {
			return "ModelNodes.isSatisfiedByProjection(" + type + ", " + spec + ")";
		}
	}

	/**
	 * Returns a predicate filtering model nodes by the specified parent path.
	 *
	 * @param parentPath  the parent path to match model nodes
	 * @return a predicate matching model nodes by parent path, never null.
	 */
	// TODO: Rename to directDescendantOf
	public static Predicate<ModelNode> withParent(ModelPath parentPath) {
		return new WithParentPredicate(parentPath);
	}

	@EqualsAndHashCode(callSuper = false)
	private static final class WithParentPredicate extends AbstractModelNodePredicate {
		private final ModelPath parentPath;

		public WithParentPredicate(ModelPath parentPath) {
			this.parentPath = parentPath;
		}

		@Override
		public boolean test(ModelNode node) {
			val parentPath = node.getPath().getParent();
			return parentPath.isPresent() && parentPath.get().equals(this.parentPath);
		}

		@Override
		public String toString() {
			return "ModelNodes.withParent(" + parentPath + ")";
		}
	}

	/**
	 * Returns a predicate that select the specified path in the model.
	 *
	 * @param path  the path to match a model node
	 * @return a predicate matching a single model node of the specified path, never null.
	 */
	// TODO: Maybe rename to pathOf(
	public static Predicate<ModelNode> withPath(ModelPath path) {
		return new WithPathPredicate(path);
	}

	@EqualsAndHashCode(callSuper = false)
	private static final class WithPathPredicate extends AbstractModelNodePredicate {
		private final ModelPath path;

		public WithPathPredicate(ModelPath path) {
			this.path = path;
		}

		@Override
		public boolean test(ModelNode node) {
			return node.getPath().equals(path);
		}

		@Override
		public String toString() {
			return "ModelNodes.withPath(" + path + ")";
		}
	}

	private static IllegalArgumentException objectNotDecoratedWithModelNode(Object target) {
		return new IllegalArgumentException(String.format("Object '%s' is not decorated with ModelNode.", target));
	}
}
