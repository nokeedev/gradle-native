package dev.nokee.model.internal.core;

import dev.nokee.model.internal.type.ModelType;
import lombok.val;
import org.gradle.api.plugins.ExtensionAware;

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
		return node -> node.canBeViewedAs(type);
	}

	/**
	 * Returns a predicate filtering the model node by the specified state.
	 *
	 * @param state  the state to filter model node
	 * @return a predicate matching model node by state
	 */
	public static Predicate<ModelNode> stateAtLeast(ModelNode.State state) {
		return node -> node.isAtLeast(state);
	}

	private static IllegalArgumentException objectNotDecoratedWithModelNode(Object target) {
		return new IllegalArgumentException(String.format("Object '%s' is not decorated with ModelNode.", target));
	}
}
