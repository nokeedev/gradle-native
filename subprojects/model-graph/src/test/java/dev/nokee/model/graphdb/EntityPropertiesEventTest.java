package dev.nokee.model.graphdb;

import dev.nokee.model.graphdb.events.EventListener;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static dev.nokee.model.graphdb.events.PropertyChangedEvent.builder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class EntityPropertiesEventTest {
	private static final Object DEFAULT_VALUE = new Object();
	private final Graph graph = Mockito.mock(Graph.class);
	private final EventListener eventListener = Mockito.mock(EventListener.class);
	private final EntityProperties subject = new EntityProperties(42, GraphEventNotifier.builder().graph(graph).listener(eventListener).build());

	@Test
	void firePropertyChangedEventForNewValue() {
		subject.put("key", "foo");
		verify(eventListener).propertyChanged(builder().graph(graph).entityId(42).key("key").previousValue(null).value("foo").build());
	}

	@Test
	void firePropertyChangedEventForExistingValue() {
		// when:
		subject.put("key", "oldFoo");
		subject.put("key", "newFoo");

		// then:
		val inOrder = Mockito.inOrder(eventListener);
		inOrder.verify(eventListener)
			.propertyChanged(builder().graph(graph).entityId(42).key("key").previousValue(null).value("oldFoo").build());
		inOrder.verify(eventListener)
			.propertyChanged(builder().graph(graph).entityId(42).key("key").previousValue("oldFoo").value("newFoo").build());
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	void doesNotFirePropertyChangedEventWhenGettingMissingValueWithDefault() {
		subject.getOrDefault("key", DEFAULT_VALUE);
		verifyNoInteractions(eventListener);
	}

	@Test
	void doesNotFirePropertyChangedEventWhenGettingExistingValue() {
		// given:
		subject.put("key", "foo");
		Mockito.reset(eventListener);

		// when:
		subject.get("key");

		// then:
		verifyNoInteractions(eventListener);
	}
}
