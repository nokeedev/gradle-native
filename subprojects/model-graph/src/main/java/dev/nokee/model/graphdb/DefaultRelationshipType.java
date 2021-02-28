package dev.nokee.model.graphdb;

import lombok.EqualsAndHashCode;

import static java.util.Objects.requireNonNull;

@EqualsAndHashCode
final class DefaultRelationshipType implements RelationshipType {
	private final String name;

	DefaultRelationshipType(String name) {
		this.name = requireNonNull(name);
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}
}
