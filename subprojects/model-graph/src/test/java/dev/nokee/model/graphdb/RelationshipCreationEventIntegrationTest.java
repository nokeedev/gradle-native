package dev.nokee.model.graphdb;

import dev.nokee.model.graphdb.events.EventListener;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static dev.nokee.model.graphdb.RelationshipType.withName;
import static dev.nokee.model.graphdb.events.RelationshipCreatedEvent.builder;
import static org.mockito.Mockito.verify;

class RelationshipCreationEventIntegrationTest {
	private final EventListener eventListener = Mockito.mock(EventListener.class);
	private final Graph graph = new DefaultGraph(eventListener);
	private final Node startNode = graph.createNode();
	private final Node endNode = graph.createNode();

	@Test
	void fireRelationshipCreatedEvent() {
		val relationship = graph.createRelationship(startNode, withName("owns"), endNode);
		verify(eventListener).relationshipCreated(builder().graph(graph).relationshipId(relationship.getId()).build());
	}
}
