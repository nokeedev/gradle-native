package dev.nokee.runtime.core;

import com.google.common.collect.ImmutableSet;
import lombok.EqualsAndHashCode;
import lombok.val;

import java.util.Iterator;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;

@EqualsAndHashCode
final class DefaultCoordinateSet<T> implements CoordinateSet<T> {
	private final Set<Coordinate<T>> coordinates;

	public DefaultCoordinateSet(Set<? extends Coordinate<T>> coordinates) {
		checkArgument(!coordinates.isEmpty(), "coordinate set cannot be empty");
		checkArgument(hasSameAxis(coordinates), "all coordinate in a set has to be for the same axis");
		this.coordinates = ImmutableSet.copyOf(coordinates);
	}

	private static boolean hasSameAxis(Iterable<? extends Coordinate<?>> coordinates) {
		val iter = coordinates.iterator();
		val firstCoordinate = iter.next();
		while (iter.hasNext()) {
			val coordinate = iter.next();
			if (!coordinate.getAxis().equals(firstCoordinate.getAxis())) {
				return false;
			}
		}
		return true;
	}

	@Override
	public Iterator<Coordinate<T>> iterator() {
		return coordinates.iterator();
	}
}
