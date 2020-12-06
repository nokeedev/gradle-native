package dev.nokee.model.internal.registry;

import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.core.ModelProjection;
import dev.nokee.model.internal.type.ModelType;
import lombok.val;
import org.gradle.api.plugins.ExtensionAware;

import java.util.function.Supplier;

/**
 * A projection that inject the model node instance.
 * The injection is done through the {@link ExtensionAware} implementation.
 */
public final class ModelNodeDecoratingModelProjection implements ModelProjection {
	private final ModelProjection delegate;
	private final Supplier<ModelNode> modelNodeSupplier;

	public ModelNodeDecoratingModelProjection(ModelProjection delegate, Supplier<ModelNode> modelNodeSupplier) {
		this.delegate = delegate;
		this.modelNodeSupplier = modelNodeSupplier;
	}

	@Override
	public <T> boolean canBeViewedAs(ModelType<T> type) {
		return delegate.canBeViewedAs(type);
	}

	@Override
	public <T> T get(ModelType<T> type) {
		return injectModelNode(delegate.get(type));
	}

	private <T> T injectModelNode(T target) {
		if (target instanceof ExtensionAware) {
			val node = ((ExtensionAware) target).getExtensions().findByType(ModelNode.class);
			if (node == null) {
				ModelNodes.inject(target, modelNodeSupplier.get());
			} else if (!node.equals(modelNodeSupplier.get())) {
				throw new IllegalStateException("");
			}
		}
		return target;
	}
}
