package dev.nokee.model.graphdb.events;

import dev.nokee.model.graphdb.Graph;
import dev.nokee.model.graphdb.Relationship;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(builderClassName = "Builder")
public class RelationshipCreatedEvent {
	Graph graph;
	long relationshipId;

	public Relationship getRelationship() {
		return graph.getRelationshipById(relationshipId);
	}

	public static final class Builder {}
}
