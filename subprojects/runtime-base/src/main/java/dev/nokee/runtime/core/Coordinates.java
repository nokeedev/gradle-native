/*
 * Copyright 2021 the original author or authors.
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
package dev.nokee.runtime.core;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.collect.Streams;
import com.google.common.reflect.TypeToken;
import lombok.EqualsAndHashCode;
import lombok.val;
import org.gradle.api.reflect.TypeOf;
import org.gradle.util.GUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

public final class Coordinates {
	@SuppressWarnings("UnstableApiUsage")
	public static <T> Class<Coordinate<T>> coordinateTypeOf(Class<T> valueType) {
		return new TypeOf<Coordinate<T>>() {}.getConcreteClass();
	}

	public static <T> Optional<T> find(CoordinateTuple self, CoordinateAxis<T> axis, boolean includeNested) {
		requireNonNull(axis);
		for (Coordinate<?> coordinate : self) {
			if (axis.equals(coordinate.getAxis()) && !Coordinates.isAbsentCoordinate(coordinate)) {
				@SuppressWarnings("unchecked")
				T result = (T) coordinate.getValue();
				return Optional.of(result);
			} else if (includeNested && coordinate instanceof CoordinateTuple) {
				val result = find((CoordinateTuple) coordinate, axis, true);
				if (result.isPresent()) {
					return result;
				}
			}
		}
		return Optional.empty();
	}

	public static Stream<Coordinate<?>> flatten(Coordinate<?> coordinate) {
		if (coordinate instanceof CoordinateTuple) {
			return Streams.stream((CoordinateTuple) coordinate).flatMap(Coordinates::flatten);
		}
		return Stream.of(coordinate);
	}

	public static Stream<CoordinateAxis<?>> standardBasis(CoordinateSpace space) {
		return Streams.stream(space)
			.flatMap(Streams::stream)
			.flatMap(Coordinates::flatten)
			.distinct() // Removes coordinate duplicates, i.e. keep only one Windows OS Family, etc.
			.collect(groupingBy(Coordinate::getAxis))
			.entrySet()
			.stream()
			.filter(it -> it.getValue().size() > 1)
			.map(Map.Entry::getKey);
	}

	@SuppressWarnings("unchecked")
	static <T> CoordinateAxis<T> inferCoordinateAxisFromCoordinateImplementation(Coordinate<T> self) {
		try {
			val getAxisMethod = self.getClass().getMethod("getValue");
			val returnType = getAxisMethod.getGenericReturnType();
			val resolvedReturnType = TypeToken.of(self.getClass()).resolveType(returnType);
			return CoordinateAxis.of((Class<T>) resolvedReturnType.getRawType());
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	static <T> T inferCoordinateValueFromCoordinateImplementation(Coordinate<T> self) {
		try {
			val getAxisMethod = self.getClass().getMethod("getValue");
			val returnType = getAxisMethod.getGenericReturnType();
			val resolvedReturnType = TypeToken.of(self.getClass()).resolveType(returnType);
			if (resolvedReturnType.isSupertypeOf(self.getClass())) {
				return (T) self;
			} else {
				throw new UnsupportedOperationException(String.format("Please implement Coordinate#getValue() for %s.", self.getClass()));
			}
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}

	static String inferCoordinateAxisNameFromType(Class<?> type) {
		return GUtil.toWords(type.getSimpleName(), '-');
	}

	static String inferCoordinateAxisDisplayNameFromType(Class<?> type) {
		return GUtil.toWords(type.getSimpleName());
	}

	public static <T> Coordinate<T> absentCoordinate(CoordinateAxis<T> axis) {
		return new AbsentCoordinate<>(axis);
	}

	public static boolean isAbsentCoordinate(Coordinate<?> coordinate) {
		return coordinate instanceof AbsentCoordinate;
	}

	@EqualsAndHashCode
	private static final class AbsentCoordinate<T> implements Coordinate<T> {
		private final CoordinateAxis<T> axis;

		public AbsentCoordinate(CoordinateAxis<T> axis) {
			this.axis = axis;
		}

		@Override
		public CoordinateAxis<T> getAxis() {
			return axis;
		}

		@Override
		public T getValue() {
			throw new UnsupportedOperationException("Absent coordinate does not have any value.");
		}

		@Override
		public String toString() {
			return "absent value for " + axis;
		}
	}

	private static final Collector<Coordinate<?>, ?, CoordinateTuple> TO_COORDINATE_TUPLE = new Collector<Coordinate<?>, ImmutableList.Builder<Coordinate<?>>, CoordinateTuple>() {
		@Override
		public Supplier<ImmutableList.Builder<Coordinate<?>>> supplier() {
			return ImmutableList::builder;
		}

		@Override
		public BiConsumer<ImmutableList.Builder<Coordinate<?>>, Coordinate<?>> accumulator() {
			return ImmutableList.Builder::add;
		}

		@Override
		public BinaryOperator<ImmutableList.Builder<Coordinate<?>>> combiner() {
			return (left, right) -> left.addAll(right.build());
		}

		@Override
		public Function<ImmutableList.Builder<Coordinate<?>>, CoordinateTuple> finisher() {
			return builder -> new DefaultCoordinateTuple(builder.build());
		}

		@Override
		public Set<Characteristics> characteristics() {
			return Collections.emptySet();
		}
	};

	public static Collector<Coordinate<?>, ?, CoordinateTuple> toCoordinateTuple() {
		return TO_COORDINATE_TUPLE;
	}

	public static <T> Collector<T, ?, CoordinateSet<T>> toCoordinateSet(CoordinateAxis<T> axis) {
		requireNonNull(axis);
		return new Collector<T, ImmutableSet.Builder<Coordinate<T>>, CoordinateSet<T>>() {
			@Override
			public Supplier<ImmutableSet.Builder<Coordinate<T>>> supplier() {
				return ImmutableSet::builder;
			}

			@Override
			public BiConsumer<ImmutableSet.Builder<Coordinate<T>>, T> accumulator() {
				return (builder, t) -> builder.add(axis.create(t));
			}

			@Override
			public BinaryOperator<ImmutableSet.Builder<Coordinate<T>>> combiner() {
				return (left, right) -> left.addAll(right.build());
			}

			@Override
			public Function<ImmutableSet.Builder<Coordinate<T>>, CoordinateSet<T>> finisher() {
				return builder -> new DefaultCoordinateSet<>(builder.build());
			}

			@Override
			public Set<Characteristics> characteristics() {
				return Sets.immutableEnumSet(Characteristics.UNORDERED);
			}
		};
	}

	public static <T> Collector<Coordinate<T>, ?, CoordinateSet<T>> toCoordinateSet() {
		return new Collector<Coordinate<T>, ImmutableSet.Builder<Coordinate<T>>, CoordinateSet<T>>() {
			@Override
			public Supplier<ImmutableSet.Builder<Coordinate<T>>> supplier() {
				return ImmutableSet::builder;
			}

			@Override
			public BiConsumer<ImmutableSet.Builder<Coordinate<T>>, Coordinate<T>> accumulator() {
				return ImmutableSet.Builder::add;
			}

			@Override
			public BinaryOperator<ImmutableSet.Builder<Coordinate<T>>> combiner() {
				return (left, right) -> left.addAll(right.build());
			}

			@Override
			public Function<ImmutableSet.Builder<Coordinate<T>>, CoordinateSet<T>> finisher() {
				return builder -> new DefaultCoordinateSet<>(builder.build());
			}

			@Override
			public Set<Characteristics> characteristics() {
				return Sets.immutableEnumSet(Characteristics.UNORDERED);
			}
		};
	}

	@SuppressWarnings("unchecked")
	public static <T> Coordinate<T> of(T obj) {
		if (obj instanceof Coordinate) {
			return (Coordinate<T>) obj;
		} else {
			val axisFields = Streams.concat(stream(obj.getClass().getFields()), stream(obj.getClass().getDeclaredFields()))
				.distinct().filter(isCoordinateAxisField()).collect(toList());

			if (axisFields.isEmpty()) {
				return CoordinateAxis.of((Class<T>) obj.getClass()).create(obj);
			} else {
				val publicAxisFields = axisFields.stream().filter(isConstantField()).collect(toList());
				if (publicAxisFields.size() > 1) {
					throw new IllegalArgumentException(String.format("Multiple coordinate axis found in %s hierarchy. Please use Coordinate.of(CoordinateAxis, T) instead.", obj.getClass()));
				}

				val it = publicAxisFields.iterator();
				if (!it.hasNext()) {
					throw new IllegalArgumentException(String.format("Coordinate axis found in %s hierarchy are not public constants. Verify the CoordinateAxis constant is accessible in class hierarchy or use Coordinate.of(CoordinateAxis, T).", obj.getClass()));
				}

				val field = it.next();
				try {
					return Coordinate.of((CoordinateAxis<T>) field.get(null), obj);
				} catch (IllegalAccessException e) {
					throw new IllegalArgumentException(String.format("Coordinate axis %s is not accessible.", field), e);
				}
			}
		}
	}

	private static Predicate<Field> isCoordinateAxisField() {
		return field -> field.getType().isAssignableFrom(CoordinateAxis.class);
	}

	private static final int CONSTANT_FIELD_MODIFIERS = Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL;
	private static Predicate<Field> isConstantField() {
		return field -> (field.getModifiers() & CONSTANT_FIELD_MODIFIERS) == CONSTANT_FIELD_MODIFIERS;
	}

	// TODO: Provide equals implementation
	// TODO: Provide sorting implementation...
}
