package dev.nokee.model.internal;

import dev.nokee.model.core.ModelNode;
import dev.nokee.model.graphdb.Graph;
import dev.nokee.model.graphdb.Node;

public final class DefaultModelNodeFactory implements ModelNodeFactory {
	private final Graph graph;

	public DefaultModelNodeFactory(Graph graph) {
		this.graph = graph;
	}

	@Override
	public ModelNode create(Node node) {
		return new DefaultModelNode(graph, node);
	}
}
