package dev.nokee.model.internal;

import dev.nokee.model.core.ModelNode;
import dev.nokee.model.core.ModelProjection;
import dev.nokee.model.graphdb.Node;

public interface ModelFactory {
	ModelNode createNode(Node node);
	ModelProjection createProjection(Node node);
}
