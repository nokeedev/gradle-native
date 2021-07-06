package dev.nokee.model.internal;

import dev.nokee.model.core.ModelProjection;
import dev.nokee.model.graphdb.Graph;
import dev.nokee.model.graphdb.Node;

final class DefaultModelProjectionFactory implements ModelProjectionFactory {
	private final Graph graph;

	DefaultModelProjectionFactory(Graph graph) {
		this.graph = graph;
	}

	@Override
	public ModelProjection create(Node node) {
		return new DefaultModelProjection(graph, node);
	}
}
