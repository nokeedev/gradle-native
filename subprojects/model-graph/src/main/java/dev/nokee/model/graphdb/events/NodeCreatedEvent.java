package dev.nokee.model.graphdb.events;

import dev.nokee.model.graphdb.Graph;
import dev.nokee.model.graphdb.Node;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(builderClassName = "Builder")
public class NodeCreatedEvent {
	Graph graph;
	long nodeId;

	public Node getNode() {
		return graph.getNodeById(nodeId);
	}

	public static final class Builder {}
}
