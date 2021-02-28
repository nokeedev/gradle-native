package dev.nokee.model.graphdb.events;

import dev.nokee.model.graphdb.Graph;
import dev.nokee.model.graphdb.Relationship;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class RelationshipCreatedEventTest {
	private final Graph graph = Mockito.mock(Graph.class);
	private final RelationshipCreatedEvent subject = RelationshipCreatedEvent.builder().graph(graph).relationshipId(42).build();

	@Test
	void canGetRelationshipInstanceFromEvent() {
		val relationship = Mockito.mock(Relationship.class);
		Mockito.when(graph.getRelationshipById(42)).thenReturn(relationship);
		assertThat(subject.getRelationship(), equalTo(relationship));
	}
}
