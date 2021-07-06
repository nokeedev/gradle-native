package dev.nokee.model.internal;

import dev.nokee.model.core.ModelProjection;
import dev.nokee.model.graphdb.Node;

interface ModelProjectionFactory {
	ModelProjection create(Node node);
}
