package dev.nokee.model.internal;

import dev.nokee.model.core.*;
import dev.nokee.model.graphdb.Node;

public interface ModelFactory {
	ModelNode createNode(Node node);
	ModelProjection createProjection(Node node);
	<T> ModelObject<T> createObject(TypeAwareModelProjection<T> projection);
}
