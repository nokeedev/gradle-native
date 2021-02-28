package dev.nokee.model.graphdb.events;

import dev.nokee.model.graphdb.Entity;
import dev.nokee.model.graphdb.Graph;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class PropertyChangedEventTest {
	private final Graph graph = Mockito.mock(Graph.class);
	private final PropertyChangedEvent subject = PropertyChangedEvent.builder().graph(graph).entityId(42).key("key").previousValue("old-value").value("new-value").build();

	@Test
	void canGetEntityInstanceFromEvent() {
		val entity = Mockito.mock(Entity.class);
		Mockito.when(graph.getEntityById(42)).thenReturn(entity);
		assertThat(subject.getEntity(), equalTo(entity));
	}
}
