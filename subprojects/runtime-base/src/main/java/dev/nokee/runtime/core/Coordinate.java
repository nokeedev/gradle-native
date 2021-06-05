package dev.nokee.runtime.core;

/**
 * Represent a value with its associated axis coordinate.
 * Multiple coordinate forms a coordinate tuple.
 *
 * @param <T>  the value type
 */
public interface Coordinate<T> {
	/**
	 * Returns the axis for this value.
	 *
	 * @return the axis, never null
	 */
	default CoordinateAxis<T> getAxis() {
		return Coordinates.getAxis(this);
	}

	/**
	 * Returns the coordinate value.
	 *
	 * @return the value, never null
	 */
	default T getValue() {
		return Coordinates.getValue(this);
	}

	/**
	 * Creates a coordinate for the specified axis and value.
	 *
	 * @param axis  the axis for the value, must not be null
	 * @param value  the coordinate value, must not be null
	 * @param <T>  the value type
	 * @return a coordinate, never null
	 */
	static <T> Coordinate<T> of(CoordinateAxis<T> axis, T value) {
		return new DefaultCoordinate<>(axis, value);
	}
}
