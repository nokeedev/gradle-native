package dev.nokee.runtime.core;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public interface CoordinateTuple extends Iterable<Coordinate<?>> {
	/**
	 * Returns the coordinate value for the specified axis, if available.
	 *
	 * If nested subspace coordinate needs to be searched, use {@link Coordinates#find(CoordinateTuple, CoordinateAxis, boolean)}.
	 * Else, it's possible to flatten the tuple to remove parent coordinate from the search.
	 *
	 * @param axis  the axis to retrieve the value, must not be null
	 * @param <T>  the axis value type
	 * @return the value for the specified axis, never null
	 */
	default <T> Optional<T> find(CoordinateAxis<T> axis) {
		return Coordinates.find(this, axis, false);
	}

	static CoordinateTuple of(Coordinate<?>... coordinates) {
		return of(Arrays.asList(coordinates));
	}

	static CoordinateTuple of(List<? extends Coordinate<?>> coordinates) {
		return new DefaultCoordinateTuple(coordinates);
	}
}
