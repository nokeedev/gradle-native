package dev.nokee.model.graphdb.events;

import dev.nokee.model.graphdb.Graph;
import dev.nokee.model.graphdb.Node;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class LabelAddedEventTest {
	private final Graph graph = Mockito.mock(Graph.class);
	private final LabelAddedEvent subject = LabelAddedEvent.builder().graph(graph).nodeId(42).build();

	@Test
	void canGetNodeInstanceFromEvent() {
		val node = Mockito.mock(Node.class);
		Mockito.when(graph.getNodeById(42)).thenReturn(node);
		assertThat(subject.getNode(), equalTo(node));
	}
}
