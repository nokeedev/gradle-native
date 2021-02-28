package dev.nokee.model.graphdb;

public interface Relationship extends Entity {
	Node getStartNode();
	Node getOtherNode(Node node);
	Node getEndNode();
	RelationshipType getType();
	boolean isType(RelationshipType type);
	Node[] getNodes();

	@Override
	Relationship property(String key, Object value);
}
