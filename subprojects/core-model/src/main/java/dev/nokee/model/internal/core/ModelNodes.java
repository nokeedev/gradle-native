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
	 * Returns the decorated model node of the specified instance.
	 * It is safe to say that for managed model node projection instance, a model node will be present.
	 *
	 * @param target  the instance to cross-reference with it's model node
	 * @return the model node for the specified instance if available
	 */
	public static ModelNode of(Object target) {
		if (target instanceof ExtensionAware) {
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
	 * Returns a predicate filtering the model node by the specified type.
	 *
	 * @param type  the type to filter model node
	 * @return a predicate matching model node by type
	 */
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
	 * Returns a predicate filtering the model node by the specified state.
	 *
	 * @param state  the state to filter model node
	 * @return a predicate matching model node by state
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
	 * Returns a predicate filtering the model node that satisfy the specified spec for the projection type specified.
	 *
	 * @param type  the projection type to match using the spec
	 * @param spec  the spec to satisfy using a projection of the model node
	 * @param <T> the projection type
	 * @return a predicate matching model node by spec of a projection
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

	private static IllegalArgumentException objectNotDecoratedWithModelNode(Object target) {
		return new IllegalArgumentException(String.format("Object '%s' is not decorated with ModelNode.", target));
	}
}
