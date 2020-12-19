package dev.nokee.model.internal.core;

import lombok.val;
import org.gradle.api.plugins.ExtensionAware;

import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public final class ModelNodeContext {
	private static final ThreadLocal<ModelNode> MODEL_NODE_INFO = new ThreadLocal<>();
	private final ModelNode node;

	private ModelNodeContext(ModelNode node) {
		this.node = requireNonNull(node);
	}

	public static ModelNodeContext of(ModelNode node) {
		return new ModelNodeContext(node);
	}

	public <T> T execute(Function<? super ModelNode, ? extends T> action) {
		val previousNode = MODEL_NODE_INFO.get();
		MODEL_NODE_INFO.set(node);
		try {
			return action.apply(node);
		} finally {
			MODEL_NODE_INFO.set(previousNode);
		}
	}

	public static ModelNode getCurrentModelNode() {
		return requireNonNull(MODEL_NODE_INFO.get(), "no mode node context");
	}

	public static <T> T injectCurrentModelNodeIfAllowed(T target) {
		requireNonNull(target);
		if (target instanceof ExtensionAware && canInjectCurrentModelNode()) {
			ModelNodes.inject(target, ModelNodeContext.getCurrentModelNode());
		}
		return target;
	}

	private static boolean canInjectCurrentModelNode() {
		return MODEL_NODE_INFO.get() != null;
	}
}
