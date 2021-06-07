package dev.nokee.runtime.core;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import lombok.EqualsAndHashCode;
import lombok.val;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.stream.Collectors.joining;

@EqualsAndHashCode
final class DefaultCoordinateTuple implements CoordinateTuple {
	private final List<Coordinate<?>> list;

	DefaultCoordinateTuple(List<? extends Coordinate<?>> list) {
		checkArgument(!list.isEmpty(), "coordinates cannot be empty");
		checkArgument(!hasDuplicatedAxis(list), "coordinates cannot contains duplicated axis");
		this.list = ImmutableList.copyOf(list);
	}

	private static boolean hasDuplicatedAxis(Iterable<? extends Coordinate<?>> coordinates) {
		val knownAxis = new HashSet<>();
		for (Coordinate<?> coordinate : coordinates) {
			if (!knownAxis.add(coordinate.getAxis())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Iterator<Coordinate<?>> iterator() {
		return list.iterator();
	}

	@Override
	public String toString() {
		return "(" + list.stream().map(it -> it.getValue().toString()).collect(joining(", ")) + ")";
	}
}
