package dev.nokee.model.graphdb.events;

import dev.nokee.model.graphdb.Graph;
import dev.nokee.model.graphdb.Label;
import dev.nokee.model.graphdb.Node;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(builderClassName = "Builder")
public class LabelAddedEvent {
	Graph graph;
	long nodeId;
	Label label;

	public Node getNode() {
		return graph.getNodeById(nodeId);
	}

	public static final class Builder {}
}
