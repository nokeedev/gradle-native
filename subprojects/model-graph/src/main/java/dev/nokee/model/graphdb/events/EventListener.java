package dev.nokee.model.graphdb.events;

public interface EventListener {
	void nodeCreated(NodeCreatedEvent event);
	void relationshipCreated(RelationshipCreatedEvent event);
	void propertyChanged(PropertyChangedEvent event);
}
