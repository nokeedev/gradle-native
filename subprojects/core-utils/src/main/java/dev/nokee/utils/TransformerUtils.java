package dev.nokee.utils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import lombok.EqualsAndHashCode;
import org.gradle.api.Action;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public final class TransformerUtils {
	private TransformerUtils() {}

	@SuppressWarnings("unchecked")
	public static <T> Transformer<T, T> noOpTransformer() {
		return (Transformer<T, T>) NoOpTransformer.INSTANCE;
	}

	private enum NoOpTransformer implements Transformer<Object, Object> {
		INSTANCE;

		@Override
		public Object transform(Object o) {
			return o;
		}

		@Override
		public String toString() {
			return "TransformerUtils.noOpTransformer()";
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> Transformer<List<T>, Iterable<T>> toListTransformer() {
		return (Transformer<List<T>, Iterable<T>>) (Transformer<? extends List<T>, ? super Iterable<T>>) ToListTransformer.INSTANCE;
	}

	private enum ToListTransformer implements Transformer<List<? extends Object>, Iterable<? extends Object>> {
		INSTANCE;

		@Override
		public List<?> transform(Iterable<?> objects) {
			return ImmutableList.copyOf(objects);
		}


		@Override
		public String toString() {
			return "TransformerUtils.toListTransformer()";
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> Transformer<Set<T>, Iterable<T>> toSetTransformer() {
		return (Transformer<Set<T>, Iterable<T>>) (Transformer<? extends Set<T>, ? super Iterable<T>>) ToSetTransformer.INSTANCE;
	}

	@SuppressWarnings("unchecked")
	public static <OUT, IN> Transformer<Set<OUT>, Iterable<IN>> toSetTransformer(Class<OUT> type) {
		return (Transformer<Set<OUT>, Iterable<IN>>) (Transformer<? extends Set<OUT>, ? super Iterable<IN>>) ToSetTransformer.INSTANCE;
	}

	private enum ToSetTransformer implements Transformer<Set<? extends Object>, Iterable<? extends Object>> {
		INSTANCE;

		@Override
		public Set<?> transform(Iterable<?> objects) {
			return ImmutableSet.copyOf(objects);
		}


		@Override
		public String toString() {
			return "TransformerUtils.toSetTransformer()";
		}
	}

	@SuppressWarnings("unchecked")
	public static <T, U> Transformer<T, U> constant(T value) {
		return (Transformer<T, U>) new ConstantTransformer<>(value);
	}

	@EqualsAndHashCode
	private static final class ConstantTransformer<T> implements Transformer<T, Object> {
		private final T value;

		public ConstantTransformer(T value) {
			this.value = value;
		}

		@Override
		public T transform(Object o) {
			return value;
		}

		@Override
		public String toString() {
			return "TransformerUtils.constant(" + value + ")";
		}
	}

	public static <T> Transformer<T, T> configureInPlace(Action<? super T> action) {
		return new ConfigureInPlaceTransformer<>(action);
	}

	@EqualsAndHashCode
	private static final class ConfigureInPlaceTransformer<T> implements Transformer<T, T> {
		private final Action<? super T> action;

		public ConfigureInPlaceTransformer(Action<? super T> action) {
			this.action = action;
		}

		@Override
		public T transform(T t) {
			action.execute(t);
			return t;
		}

		@Override
		public String toString() {
			return "TransformerUtils.configureInPlace(" + action + ")";
		}
	}

	/**
	 * Adapts an flat element mapper to transform each elements individually of the collection.
	 * The result will apply a proper flatMap algorithm to the provided collection.
	 *
	 * @param mapper  an element mapper
	 * @param <OUT>  output element type resulting from the transform
	 * @param <IN>  input element type to transform
	 * @return a {@link Transformer} instance to flat transform each the element of an iterable, never null.
	 */
	public static <OUT, IN> Transformer<List<OUT>, Iterable<IN>> flatTransformEach(org.gradle.api.Transformer<? extends Iterable<OUT>, ? super IN> mapper) {
		return new FlatTransformEachAdapter<>(mapper);
	}

	@EqualsAndHashCode
	private static final class FlatTransformEachAdapter<OUT, IN> implements Transformer<List<OUT>, Iterable<IN>> {
		private final org.gradle.api.Transformer<? extends Iterable<OUT>, ? super IN> mapper;

		public FlatTransformEachAdapter(org.gradle.api.Transformer<? extends Iterable<OUT>, ? super IN> mapper) {
			this.mapper = requireNonNull(mapper);
		}

		@Override
		public List<OUT> transform(Iterable<IN> elements) {
			ImmutableList.Builder<OUT> result = ImmutableList.builder();
			for (IN element : elements) {
				result.addAll(mapper.transform(element));
			}
			return result.build();
		}

		@Override
		public String toString() {
			return "TransformerUtils.flatTransformEach(" + mapper + ")";
		}
	}

	/**
	 * Adapts an element mapper to transform each elements individually of the collection.
	 * The result will apply a proper map algorithm to the provided collection.
	 *
	 * @param mapper  an element mapper
	 * @param <OUT>  output element type resulting from the transform
	 * @param <IN>  input element type to transform
	 * @return a {@link Transformer} instance to transform each the element of an iterable, never null.
	 */
	public static <OUT, IN> Transformer<List<OUT>, Iterable<IN>> transformEach(org.gradle.api.Transformer<? extends OUT, ? super IN> mapper) {
		return new TransformEachAdapter<>(mapper);
	}

	@EqualsAndHashCode
	private static final class TransformEachAdapter<OUT, IN> implements Transformer<List<OUT>, Iterable<IN>> {
		private final org.gradle.api.Transformer<? extends OUT, ? super IN> mapper;

		public TransformEachAdapter(org.gradle.api.Transformer<? extends OUT, ? super IN> mapper) {
			this.mapper = requireNonNull(mapper);
		}

		@Override
		public List<OUT> transform(Iterable<IN> elements) {
			ImmutableList.Builder<OUT> result = ImmutableList.builder();
			for (IN element : elements) {
				result.add(mapper.transform(element));
			}
			return result.build();
		}

		@Override
		public String toString() {
			return "TransformerUtils.transformEach(" + mapper + ")";
		}
	}

	public static <A, B, C> Transformer<C, A> compose(org.gradle.api.Transformer<C, B> g, org.gradle.api.Transformer<? extends B, A> f) {
		return new ComposeTransformer<>(g, f);
	}

	@EqualsAndHashCode
	private static final class ComposeTransformer<A, B, C> implements Transformer<C, A> {
		private final org.gradle.api.Transformer<? extends C, ? super B> g;
		private final org.gradle.api.Transformer<? extends B, ? super A> f;

		public ComposeTransformer(org.gradle.api.Transformer<? extends C, ? super B> g, org.gradle.api.Transformer<? extends B, ? super A> f) {
			this.g = Objects.requireNonNull(g);
			this.f = Objects.requireNonNull(f);
		}

		@Override
		public C transform(A in) {
			return g.transform(f.transform(in));
		}

		@Override
		public String toString() {
			return "TransformerUtils.compose(" + g + ", " + f + ")";
		}
	}

	@FunctionalInterface
	public interface Transformer<OUT, IN> extends org.gradle.api.Transformer<OUT, IN> {
		default <V> Transformer<OUT, V> compose(Transformer<? extends IN, ? super V> before) {
			return new ComposeTransformer<>(this, before);
		}

		default <V> Transformer<V, IN> andThen(Transformer<? extends V, ? super OUT> after) {
			return new ComposeTransformer<>(after, this);
		}
	}
}
