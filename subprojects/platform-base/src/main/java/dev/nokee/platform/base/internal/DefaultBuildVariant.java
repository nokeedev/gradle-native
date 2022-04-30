/*
 * Copyright 2020-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.nokee.platform.base.internal;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import dev.nokee.runtime.core.Coordinate;
import dev.nokee.runtime.core.CoordinateAxis;
import dev.nokee.runtime.core.CoordinateSpace;
import dev.nokee.runtime.core.CoordinateTuple;
import dev.nokee.runtime.core.Coordinates;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.val;
import org.gradle.api.GradleException;
import org.gradle.api.Named;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.google.common.collect.Streams.stream;
import static dev.nokee.runtime.core.Coordinates.toCoordinateTuple;
import static java.util.stream.Collectors.toList;

@Value
public class DefaultBuildVariant implements BuildVariantInternal {
	CoordinateTuple coordinates;
	@EqualsAndHashCode.Exclude Dimensions allDimensions;
	@EqualsAndHashCode.Exclude Dimensions ambiguousDimensions;
	@EqualsAndHashCode.Exclude String name;

	public static DefaultBuildVariant of(Coordinate<?>... coordinates) {
		val tuple = CoordinateTuple.of(coordinates);
		val allDimensions = Dimensions.of(stream(tuple).flatMap(Coordinates::flatten).map(named()).filter(nonEmpty()).collect(toList()));
		return new DefaultBuildVariant(tuple, allDimensions, allDimensions, allDimensions.getAsLowerCamelCase().orElse(""));
	}

	public static List<DefaultBuildVariant> fromSpace(CoordinateSpace space) {
		val visibleAxis = Coordinates.standardBasis(space).collect(ImmutableSet.toImmutableSet());
		return stream(space).map(it -> {
			val allDimensions = Dimensions.of(stream(it).flatMap(Coordinates::flatten).map(named()).filter(nonEmpty()).collect(toList()));
			val dimensions = Dimensions.of(stream(it).flatMap(Coordinates::flatten).filter(onlyAxis(visibleAxis)).map(named()).filter(nonEmpty()).collect(toList()));
			return new DefaultBuildVariant(it, allDimensions, dimensions, dimensions.getAsLowerCamelCase().orElse(""));
		}).collect(toList());
	}

	private static Predicate<String> nonEmpty() {
		return it -> !it.isEmpty();
	}

	private static Predicate<Coordinate<?>> onlyAxis(Set<CoordinateAxis<?>> visibleAxis) {
		return it -> visibleAxis.contains(it.getAxis());
	}

	private static Function<Coordinate<?>, String> named() {
		return it -> {
			if (Coordinates.isAbsentCoordinate(it)) {
				return "";
			} else {
				val value = it.getValue();
				if (value instanceof Named) {
					return ((Named) value).getName();
				}
			}
			throw new UnsupportedOperationException("Need to be named");
		};
	}

	@Override
	public List<Coordinate<?>> getDimensions() {
		return stream(coordinates).flatMap(Coordinates::flatten).collect(toList());
	}

	@Override
	public <T> T getAxisValue(CoordinateAxis<T> type) {
		// TODO: We can validate the type of the value match the type of the dimension.
		return Coordinates.find(coordinates, type, true).orElseThrow(() -> new GradleException(String.format("Dimension '%s' is not part of this build variant.", type.toString())));
	}

	@Override
	public <T> Optional<T> findAxisValue(CoordinateAxis<T> type) {
		// TODO: We can validate the type of the value match the type of the dimension.
		return Coordinates.find(coordinates, type, true);
	}

	@Override
	public boolean hasAxisValue(CoordinateAxis<?> type) {
		return Coordinates.find(coordinates, type, true).isPresent();
	}

	@Override
	public BuildVariantInternal withoutDimension(CoordinateAxis<?> type) {
		val newCoordinates = stream(coordinates).filter(it -> !type.equals(it.getAxis())).collect(toCoordinateTuple());
		return new DefaultBuildVariant(newCoordinates, Dimensions.empty(), Dimensions.empty(), "");
	}

	@Override
	public boolean hasAxisOf(Object axisValue) {
		if (axisValue instanceof Coordinate) {
			val type = ((Coordinate<?>) axisValue).getAxis();
			Optional<Boolean> result = Coordinates.find(coordinates, type, true).map(it -> Objects.equals(it, ((Coordinate<?>) axisValue).getValue()));
			return result.orElse(false);
		} else {
			return Streams.stream(coordinates).flatMap(Coordinates::flatten).anyMatch(it -> !Coordinates.isAbsentCoordinate(it) && Objects.equals(it.getValue(), axisValue));
		}
	}

	@Override
	public String toString() {
		return name;
	}
}
