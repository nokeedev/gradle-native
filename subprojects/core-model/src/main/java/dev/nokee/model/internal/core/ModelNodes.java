package dev.nokee.model.internal.core;

import lombok.val;
import org.gradle.api.plugins.ExtensionAware;

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

	private static IllegalArgumentException objectNotDecoratedWithModelNode(Object target) {
		return new IllegalArgumentException(String.format("Object '%s' is not decorated with ModelNode.", target));
	}
}
