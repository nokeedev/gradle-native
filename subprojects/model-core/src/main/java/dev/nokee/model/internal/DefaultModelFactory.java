package dev.nokee.model.internal;

import dev.nokee.model.core.ModelNode;
import dev.nokee.model.core.ModelProjection;
import dev.nokee.model.graphdb.Graph;
import dev.nokee.model.graphdb.Node;
import org.gradle.api.model.ObjectFactory;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public final class DefaultModelFactory implements ModelFactory {
	private final Map<Node, ModelNode> nodeCache = new HashMap<>();
	private final Map<Node, ModelProjection> projectionCache = new HashMap<>();
	private final Graph graph;
	@Nullable private final ObjectFactory objectFactory;
	@Nullable private final NamedDomainObjectRegistry registry;

	public DefaultModelFactory(Graph graph) {
		this(graph, null, new DefaultNamedDomainObjectRegistry());
	}

	public DefaultModelFactory(Graph graph, @Nullable ObjectFactory objectFactory, @Nullable NamedDomainObjectRegistry registry) {
		this.graph = requireNonNull(graph);
		this.objectFactory = objectFactory;
		this.registry = registry;
	}

	@Override
	public ModelNode createNode(Node node) {
		return nodeCache.computeIfAbsent(node, n -> new DefaultModelNode(this, objectFactory, graph, node, registry));
	}

	@Override
	public ModelProjection createProjection(Node node) {
		return projectionCache.computeIfAbsent(node, n -> new DefaultModelProjection<>(this, node));
	}
}
