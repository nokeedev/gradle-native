package dev.nokee.model.internal;

import dev.nokee.model.core.ModelNode;
import dev.nokee.model.core.ModelProjection;
import dev.nokee.model.graphdb.Graph;
import dev.nokee.model.graphdb.Node;

public final class DefaultModelFactory implements ModelFactory {
	private final Graph graph;

	public DefaultModelFactory(Graph graph) {
		this.graph = graph;
	}

	@Override
	public ModelNode createNode(Node node) {
		return new DefaultModelNode(this, graph, node);
	}

	@Override
	public ModelProjection createProjection(Node node) {
		return new DefaultModelProjection(this, node);
	}
}
