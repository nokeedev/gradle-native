package dev.nokee.model.graphdb;

public class NotFoundException extends RuntimeException {
	NotFoundException(String message) {
		super(message);
	}

	static NotFoundException createPropertyNotFoundException(String key) {
		return new NotFoundException(String.format("Property '%s' not found on this entity.", key));
	}

	static NotFoundException createEntityNotFoundException(long id) {
		return new NotFoundException(String.format("Entity '%s' not found in this graph.", id));
	}

	static NotFoundException createNodeNotFoundException(long id) {
		return new NotFoundException(String.format("Node '%s' not found in this graph.", id));
	}

	static NotFoundException createRelationshipNotFoundException(long id) {
		return new NotFoundException(String.format("Relationship '%s' not found in this graph.", id));
	}
}
