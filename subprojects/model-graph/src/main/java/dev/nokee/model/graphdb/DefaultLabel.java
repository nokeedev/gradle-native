package dev.nokee.model.graphdb;

import lombok.EqualsAndHashCode;

import static java.util.Objects.requireNonNull;

@EqualsAndHashCode
class DefaultLabel implements Label {
	private final String name;

	DefaultLabel(String name) {
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
