package dev.nokee.model.internal;

import dev.nokee.model.core.ModelNode;
import dev.nokee.model.core.ModelProjection;
import dev.nokee.model.graphdb.Graph;
import dev.nokee.model.graphdb.Node;

import java.util.HashMap;
import java.util.Map;

public final class DefaultModelFactory implements ModelFactory {
	private final Map<Node, ModelNode> nodeCache = new HashMap<>();
	private final Map<Node, ModelProjection> projectionCache = new HashMap<>();
	private final Graph graph;

	public DefaultModelFactory(Graph graph) {
		this.graph = graph;
	}

	@Override
	public ModelNode createNode(Node node) {
		return nodeCache.computeIfAbsent(node, n -> new DefaultModelNode(this, graph, node));
	}

	@Override
	public ModelProjection createProjection(Node node) {
		return projectionCache.computeIfAbsent(node, n -> new DefaultModelProjection<>(this, node));
	}
}
