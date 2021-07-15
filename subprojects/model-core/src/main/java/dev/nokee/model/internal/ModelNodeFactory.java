package dev.nokee.model.internal;

import dev.nokee.model.dsl.ModelNode;

public interface ModelNodeFactory {
	ModelNode create(dev.nokee.model.core.ModelNode node);
}
