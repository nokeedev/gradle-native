package dev.nokee.model.graphdb;

public interface RelationshipType {
	String name();

	static RelationshipType withName(String name) {
		return new DefaultRelationshipType(name);
	}
}
