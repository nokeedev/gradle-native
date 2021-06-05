package dev.nokee.runtime.core;

import dev.nokee.internal.testing.Assumptions;

import java.util.Arrays;
import java.util.HashSet;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toSet;

public class DefaultCoordinateSetTest implements CoordinateSetFactoryTester {
	@Override
	public <T extends Enum<T>> CoordinateSet<T> createSubject(Class<T> type) {
		return Assumptions.skipCurrentTestExecution("Testing new DefaultCoordinateSet(Set<Coordinate<T>>)");
	}

	@Override
	public <T> CoordinateSet<T> createSubject(Coordinate<T>... coordinates) {
		return new DefaultCoordinateSet<>(new HashSet<>(asList(coordinates)));
	}

	@Override
	public <T> CoordinateSet<T> createSubject(CoordinateAxis<T> axis, T... values) {
		return new DefaultCoordinateSet<>(Arrays.stream(values).map(axis::create).collect(toSet()));
	}
}
