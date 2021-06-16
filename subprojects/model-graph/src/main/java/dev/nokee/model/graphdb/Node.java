package dev.nokee.model.graphdb;

import java.util.Optional;
import java.util.stream.Stream;

public interface Node extends Entity {
	Relationship createRelationshipTo(Node otherNode, RelationshipType type);
	Stream<Relationship> getRelationships();
	Stream<Relationship> getRelationships(Direction direction);
	Stream<Relationship> getRelationships(Direction direction, RelationshipType... types);
	Stream<Relationship> getRelationships(RelationshipType... types);

	boolean hasRelationship();
	boolean hasRelationship(Direction direction);
	boolean hasRelationship(Direction direction, RelationshipType... types);
	boolean hasRelationship(RelationshipType... types);

	Optional<Relationship> getSingleRelationship(RelationshipType type, Direction direction);

	Node addLabel(Label label);
	Stream<Label> getLabels();
	boolean hasLabel(Label label);

	@Override
	Node property(String key, Object value);
}
