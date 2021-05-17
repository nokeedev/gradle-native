package dev.nokee.model.internal;

import dev.nokee.model.core.ModelNode;
import dev.nokee.model.graphdb.Graph;
import dev.nokee.model.registry.ModelRegistry;

final class DefaultModelRegistry implements ModelRegistry {
	private final Graph graph = Graph.builder().build();
	private final ModelNode root = new DefaultModelNode(graph, graph.createNode());

	@Override
	public ModelNode getRoot() {
		return root;
	}
}
