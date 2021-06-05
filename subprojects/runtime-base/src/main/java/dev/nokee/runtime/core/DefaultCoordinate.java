package dev.nokee.runtime.core;

import lombok.EqualsAndHashCode;

import static java.util.Objects.requireNonNull;

@EqualsAndHashCode
final class DefaultCoordinate<T> implements Coordinate<T> {
	private final CoordinateAxis<T> axis;
	private final T value;

	public DefaultCoordinate(CoordinateAxis<T> axis, T value) {
		this.axis = requireNonNull(axis);
		this.value = requireNonNull(value);
	}

	@Override
	public CoordinateAxis<T> getAxis() {
		return axis;
	}

	@Override
	public T getValue() {
		return value;
	}

	@Override
	public String toString() {
		return axis + " '" + value + "'";
	}
}
