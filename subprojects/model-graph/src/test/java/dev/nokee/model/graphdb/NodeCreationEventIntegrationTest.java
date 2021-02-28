package dev.nokee.model.graphdb;

import dev.nokee.model.graphdb.events.EventListener;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static dev.nokee.model.graphdb.events.NodeCreatedEvent.builder;
import static org.mockito.Mockito.verify;

class NodeCreationEventIntegrationTest {
	private final EventListener eventListener = Mockito.mock(EventListener.class);
	private final Graph graph = new DefaultGraph(eventListener);

	@Test
	void fireNodeCreatedEvent() {
		val node = graph.createNode();
		verify(eventListener).nodeCreated(builder().graph(graph).nodeId(node.getId()).build());
	}
}
