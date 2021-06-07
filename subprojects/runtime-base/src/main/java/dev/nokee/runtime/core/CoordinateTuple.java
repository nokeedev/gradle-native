package dev.nokee.runtime.core;

import java.util.Arrays;
import java.util.List;

public interface CoordinateTuple extends Iterable<Coordinate<?>> {
	/**
	 * Returns the coordinate value for the specified axis.
	 *
	 * @param axis  the axis to retrieve the value, must not be null
	 * @param <T>  the axis value type
	 * @return the value for the specified axis, never null
	 */
	default <T> T get(CoordinateAxis<T> axis) {
		return Coordinates.get(this, axis);
	}

	static CoordinateTuple of(Coordinate<?>... coordinates) {
		return of(Arrays.asList(coordinates));
	}

	static CoordinateTuple of(List<? extends Coordinate<?>> coordinates) {
		return new DefaultCoordinateTuple(coordinates);
	}
}
