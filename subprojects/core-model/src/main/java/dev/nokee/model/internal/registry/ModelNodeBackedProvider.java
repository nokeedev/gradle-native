package dev.nokee.model.internal.registry;

import dev.nokee.model.internal.core.ModelIdentifier;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelProvider;
import dev.nokee.model.internal.type.ModelType;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public final class ModelNodeBackedProvider<T> implements ModelProvider<T> {
	private final ModelIdentifier<T> identifier;
	private final ModelType<T> type;
	@EqualsAndHashCode.Exclude private final ModelNode node;

	public ModelNodeBackedProvider(ModelType<T> type, ModelNode node) {
		this.identifier = ModelIdentifier.of(node.getPath(), type);
		this.type = type;
		this.node = node;
	}

	@Override
	public ModelIdentifier<T> getIdentifier() {
		return identifier;
	}

	@Override
	public T get() {
		return node.get(type);
	}
}
