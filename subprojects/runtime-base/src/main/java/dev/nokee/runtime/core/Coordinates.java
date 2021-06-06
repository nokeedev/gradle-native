package dev.nokee.runtime.core;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;
import lombok.val;
import org.gradle.util.GUtil;

import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.util.Objects.requireNonNull;

final class Coordinates {

	public static <T> T get(CoordinateTuple self, CoordinateAxis<T> axis) {
		requireNonNull(axis);
		return find(self, axis).orElseThrow(() -> new IllegalArgumentException("No coordinate exists for " + axis));
	}

	private static <T> Optional<T> find(Iterable<Coordinate<?>> coordinates, CoordinateAxis<T> axis) {
		for (Coordinate<?> coordinate : coordinates) {
			if (coordinate.getAxis().equals(axis)) {
				@SuppressWarnings("unchecked")
				T result = (T) coordinate.getValue();
				return Optional.of(result);
			} else if (coordinate instanceof CoordinateTuple) {
				val result = find((CoordinateTuple) coordinate, axis);
				if (result.isPresent()) {
					return result;
				}
			}
		}
		return Optional.empty();
	}

	public static <T> CoordinateAxis<T> getAxis(Coordinate<T> self) {
		try {
			val getAxisMethod = self.getClass().getMethod("getValue");
			val returnType = getAxisMethod.getGenericReturnType();
			val resolvedReturnType = TypeToken.of(self.getClass()).resolveType(returnType);
			return CoordinateAxis.of((Class<T>)resolvedReturnType.getRawType());
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}

	public static <T> T getValue(Coordinate<T> self) {
		try {
			val getAxisMethod = self.getClass().getMethod("getValue");
			val returnType = getAxisMethod.getGenericReturnType();
			val resolvedReturnType = TypeToken.of(self.getClass()).resolveType(returnType);
			if (resolvedReturnType.isSubtypeOf(self.getClass())) {
				return (T) self;
			} else {
				throw new UnsupportedOperationException("Please implement Coordinate#getValue().");
			}
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}

	public static String inferCoordinateAxisNameFromType(Class<?> type) {
		return GUtil.toWords(type.getSimpleName(), '-');
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
