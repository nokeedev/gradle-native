package dev.nokee.runtime.core;

import java.util.Iterator;
import java.util.Set;

final class DefaultCoordinateSpace implements CoordinateSpace {
	private final Set<CoordinateTuple> coordinatesList;

	DefaultCoordinateSpace(Set<CoordinateTuple> coordinatesList) {
		this.coordinatesList = coordinatesList;
	}

	@Override
	public Iterator<CoordinateTuple> iterator() {
		return coordinatesList.iterator();
	}
}
