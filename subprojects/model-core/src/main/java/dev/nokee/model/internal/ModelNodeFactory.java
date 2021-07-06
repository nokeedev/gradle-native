package dev.nokee.model.internal;

import dev.nokee.model.core.ModelNode;
import dev.nokee.model.graphdb.Node;

public interface ModelNodeFactory {
	ModelNode create(Node node);
}
