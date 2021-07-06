package dev.nokee.model.registry;

import dev.nokee.model.core.ModelNode;
import dev.nokee.model.core.ModelProjection;
import dev.nokee.model.streams.ModelStream;

public interface ModelRegistry {
	ModelNode getRoot();

	ModelStream<ModelProjection> allProjections();
}
