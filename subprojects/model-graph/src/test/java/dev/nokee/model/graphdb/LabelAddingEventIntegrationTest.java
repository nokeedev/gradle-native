package dev.nokee.model.graphdb;

import dev.nokee.model.graphdb.events.EventListener;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static dev.nokee.model.graphdb.Label.label;
import static dev.nokee.model.graphdb.events.LabelAddedEvent.builder;
import static org.mockito.Mockito.verify;

class LabelAddingEventIntegrationTest {
	private final EventListener eventListener = Mockito.mock(EventListener.class);
	private final Graph graph = new DefaultGraph(eventListener);

	@Test
	void fireLabelAddedEvent() {
		val label = label("foo");
		val node = graph.createNode().addLabel(label);
		verify(eventListener).labelAdded(builder().graph(graph).nodeId(node.getId()).label(label).build());
	}
}
