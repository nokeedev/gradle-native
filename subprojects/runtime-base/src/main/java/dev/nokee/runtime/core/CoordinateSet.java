package dev.nokee.runtime.core;

import java.util.Set;

import static java.util.Arrays.stream;

public interface CoordinateSet<T> extends Iterable<Coordinate<T>> {

	static <E extends Enum<E>> CoordinateSet<E> of(Class<E> type) {
		return stream(type.getEnumConstants()).collect(Coordinates.toCoordinateSet(CoordinateAxis.of(type)));
	}

	static <E extends Enum<E>> CoordinateSet<E> of(CoordinateAxis<E> axis) {
		return stream(axis.getType().getEnumConstants()).collect(Coordinates.toCoordinateSet(axis));
	}

	static <T> CoordinateSet<T> of(CoordinateAxis<T> axis, T... values) {
		return stream(values).collect(Coordinates.toCoordinateSet(axis));
	}

	static <T> CoordinateSet<T> of(CoordinateAxis<T> axis, Set<T> values) {
		return values.stream().collect(Coordinates.toCoordinateSet(axis));
	}

	static <T> CoordinateSet<T> of(Coordinate<T>... coordinates) {
		return stream(coordinates).collect(Coordinates.toCoordinateSet());
	}

	static <T> CoordinateSet<T> of(Set<Coordinate<T>> coordinates) {
		return coordinates.stream().collect(Coordinates.toCoordinateSet());
	}

//	public CoordinateSpace multiply(CoordinateSet<?> set) {
//		throw new UnsupportedOperationException();
//	}

//	public CoordinateSet<?> x(CoordinateSet<?> set) {
//		throw new UnsupportedOperationException();
//	}
//
//	public CoordinateSpace toSpace() {
//		throw new UnsupportedOperationException();
//	}
}
