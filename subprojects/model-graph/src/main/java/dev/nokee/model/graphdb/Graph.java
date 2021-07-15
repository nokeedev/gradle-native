package dev.nokee.model.graphdb;

import dev.nokee.model.graphdb.events.EventListener;

import java.util.stream.Stream;

public interface Graph {

	Node createNode();

	Relationship createRelationship(Node from, RelationshipType type, Node to);

	Stream<Node> getAllNodes();

	Stream<Relationship> getAllRelationships();

	Entity getEntityById(long id);

	Node getNodeById(long id);

	Relationship getRelationshipById(long id);

	static Builder builder() {
		return new Builder();
	}

	final class Builder {
		private EventListener eventListener;

		public Builder listener(EventListener eventListener) {
			this.eventListener = eventListener;
			return this;
		}

		public Graph build() {
			if (eventListener == null) {
				return new DefaultGraph();
			}
			return new DefaultGraph(eventListener);
		}
	}
}
