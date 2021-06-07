package dev.nokee.runtime.core;

import com.google.common.collect.*;
import com.google.common.reflect.TypeToken;
import lombok.val;
import org.gradle.api.reflect.TypeOf;
import org.gradle.util.GUtil;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.groupingBy;

public final class Coordinates {
	@SuppressWarnings("UnstableApiUsage")
	public static <T> Class<Coordinate<T>> coordinateTypeOf(Class<T> valueType) {
		return new TypeOf<Coordinate<T>>() {}.getConcreteClass();
	}

	public static <T> Optional<T> find(CoordinateTuple self, CoordinateAxis<T> axis, boolean includeNested) {
		requireNonNull(axis);
		for (Coordinate<?> coordinate : self) {
			if (axis.equals(coordinate.getAxis())) {
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

	static <T> CoordinateAxis<T> inferCoordinateAxisFromCoordinateImplementation(Coordinate<T> self) {
		try {
			val getAxisMethod = self.getClass().getMethod("getValue");
			val returnType = getAxisMethod.getGenericReturnType();
			val resolvedReturnType = TypeToken.of(self.getClass()).resolveType(returnType);
			return CoordinateAxis.of((Class<T>)resolvedReturnType.getRawType());
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}

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

	// TODO: Provide equals implementation
	// TODO: Provide sorting implementation...
}
