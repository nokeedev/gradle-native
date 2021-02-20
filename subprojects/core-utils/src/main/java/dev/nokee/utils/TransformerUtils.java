package dev.nokee.utils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import lombok.EqualsAndHashCode;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.specs.Spec;
import org.gradle.internal.Transformers;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;

public final class TransformerUtils {
	private TransformerUtils() {}

	public static <OUT, IN extends OUT> Transformer<OUT, IN> noOpTransformer() {
		return Cast.uncheckedCast("OUT type is statically compatible with IN", NoOpTransformer.INSTANCE);
	}

	private enum NoOpTransformer implements Transformer<Object, Object> {
		INSTANCE;

		@Override
		public Object transform(Object o) {
			return o;
		}

		public <OUT, IN> Transformer<OUT, IN> withNarrowTypes() {
			return Cast.uncheckedCast("types already checked by caller", this);
		}

		@Override
		public String toString() {
			return "TransformerUtils.noOpTransformer()";
		}
	}

	static boolean isNoOpTransformer(org.gradle.api.Transformer<?, ?> transformer) {
		return transformer == NoOpTransformer.INSTANCE || transformer.equals(Transformers.noOpTransformer());
	}

	public static <OUT, IN extends OUT, T extends Iterable<? extends IN>> Transformer<List<OUT>, T> toListTransformer() {
		return ToListTransformer.INSTANCE.withNarrowTypes();
	}

	/** @see #toListTransformer() */
	private enum ToListTransformer implements Transformer<List<Object>, Iterable<?>> {
		INSTANCE;

		@Override
		public List<Object> transform(Iterable<?> objects) {
			return ImmutableList.copyOf(objects);
		}

		public <OUT, IN, T extends Iterable<? extends IN>> Transformer<List<OUT>, T> withNarrowTypes() {
			return Cast.uncheckedCast("types already checked by caller", this);
		}

		@Override
		public String toString() {
			return "TransformerUtils.toListTransformer()";
		}
	}

	public static <OUT, IN, T extends Iterable<? extends IN>> Transformer<List<OUT>, T> toListTransformer(Class<OUT> type) {
		if (type.equals(Object.class)) {
			return ToListTransformer.INSTANCE.withNarrowTypes();
		}
		return new ToListTransformerAdapter<>(type);
	}

	/** @see #toListTransformer(Class) */
	@EqualsAndHashCode
	private static final class ToListTransformerAdapter<OUT, IN, T extends Iterable<? extends IN>> implements Transformer<List<OUT>, T> {
		private final Class<OUT> type;

		ToListTransformerAdapter(Class<OUT> type) {
			this.type = type;
		}

		@Override
		public List<OUT> transform(T objects) {
			val builder = ImmutableList.<OUT>builder();
			for (IN object : objects) {
				builder.add(type.cast(object));
			}
			return builder.build();
		}

		@Override
		public String toString() {
			return "TransformerUtils.toListTransformer(" + type + ")";
		}
	}

	public static <OUT, IN extends OUT, T extends Iterable<? extends IN>> Transformer<Set<OUT>, T> toSetTransformer() {
		return ToSetTransformer.INSTANCE.withNarrowTypes();
	}

	/** @see #toSetTransformer() */
	private enum ToSetTransformer implements Transformer<Set<Object>, Iterable<?>> {
		INSTANCE;

		@Override
		public Set<Object> transform(Iterable<?> objects) {
			return ImmutableSet.copyOf(objects);
		}

		public <OUT, IN, T extends Iterable<? extends IN>> Transformer<Set<OUT>, T> withNarrowTypes() {
			return Cast.uncheckedCast("types already checked by caller", this);
		}

		@Override
		public String toString() {
			return "TransformerUtils.toSetTransformer()";
		}
	}

	public static <OUT, IN, T extends Iterable<? extends IN>> Transformer<Set<OUT>, T> toSetTransformer(Class<OUT> type) {
		if (type.equals(Object.class)) {
			return ToSetTransformer.INSTANCE.withNarrowTypes();
		}
		return new ToSetTransformerAdapter<>(type);
	}

	/** @see #toSetTransformer(Class) */
	@EqualsAndHashCode
	private static final class ToSetTransformerAdapter<OUT, IN, T extends Iterable<? extends IN>> implements Transformer<Set<OUT>, T> {
		private final Class<OUT> type;

		ToSetTransformerAdapter(Class<OUT> type) {
			this.type = type;
		}

		@Override
		public Set<OUT> transform(T objects) {
			val builder = ImmutableSet.<OUT>builder();
			for (IN object : objects) {
				builder.add(type.cast(object));
			}
			return builder.build();
		}

		@Override
		public String toString() {
			return "TransformerUtils.toSetTransformer(" + type + ")";
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
			this.value = requireNonNull(value);
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
			this.action = requireNonNull(action);
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
	public static <OUT, IN, T extends Iterable<? extends IN>> Transformer<Iterable<OUT>, T> flatTransformEach(org.gradle.api.Transformer<? extends Iterable<OUT>, ? super IN> mapper) {
		return new FlatTransformEachAdapter<>(mapper);
	}

	/** @see #flatTransformEach(org.gradle.api.Transformer) */
	@EqualsAndHashCode
	private static final class FlatTransformEachAdapter<OUT, IN, T extends Iterable<? extends IN>> implements Transformer<Iterable<OUT>, T> {
		private final org.gradle.api.Transformer<? extends Iterable<OUT>, ? super IN> mapper;

		public FlatTransformEachAdapter(org.gradle.api.Transformer<? extends Iterable<OUT>, ? super IN> mapper) {
			this.mapper = requireNonNull(mapper);
		}

		@Override
		public Iterable<OUT> transform(T elements) {
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
	public static <OUT, IN, T extends Iterable<? extends IN>> Transformer<Iterable<OUT>, T> transformEach(org.gradle.api.Transformer<? extends OUT, ? super IN> mapper) {
		if (isNoOpTransformer(mapper)) {
			return NoOpTransformer.INSTANCE.withNarrowTypes();
		}
		return new TransformEachAdapter<>(mapper);
	}

	/** @see #transformEach(org.gradle.api.Transformer) */
	@EqualsAndHashCode
	private static final class TransformEachAdapter<OUT, IN, T extends Iterable<? extends IN>> implements Transformer<Iterable<OUT>, T> {
		private final org.gradle.api.Transformer<? extends OUT, ? super IN> mapper;

		public TransformEachAdapter(org.gradle.api.Transformer<? extends OUT, ? super IN> mapper) {
			this.mapper = requireNonNull(mapper);
		}

		@Override
		public Iterable<OUT> transform(T elements) {
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
		if (isNoOpTransformer(g)) {
			return Cast.uncheckedCast("g is a noop transformer, so we can assume the types are matching", f);
		}

		if (isNoOpTransformer(f)) {
			return Cast.uncheckedCast("f is a noop transformer, so we can assume the types are matching", g);
		}

		return new ComposeTransformer<>(g, f);
	}

	/** @see #compose(org.gradle.api.Transformer, org.gradle.api.Transformer) */
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

	/**
	 * Returns a transformer that ignores its input and returns the result of {@code supplier.get()}.
	 *
	 * @param supplier  the supplier for the transformer's output, must not be null
	 * @param <T>  the transformer output type
	 * @return a {@link Transformer} instance that ignores its input and returns the result of the specified supplier, never null
	 */
	public static <T> Transformer<T, Object> forSupplier(Supplier<? extends T> supplier) {
		return new SupplierTransformer<>(supplier);
	}

	/** @see #forSupplier(Supplier) */
	@EqualsAndHashCode
	private static final class SupplierTransformer<T> implements Transformer<T, Object> {
		private final Supplier<? extends T> supplier;

		private SupplierTransformer(Supplier<? extends T> supplier) {
			this.supplier = requireNonNull(supplier);
		}

		@Override
		public T transform(@Nullable Object input) {
			return supplier.get();
		}

		@Override
		public String toString() {
			return "TransformerUtils.forSupplier(" + supplier + ")";
		}
	}

	/**
	 * Returns a transformer that return an iterable with only the element matching the specified specification.
	 * The result will apply a filter algorithm to the provided collection.
	 *
	 * @param spec  a filter spec, must not be null
	 * @param <T>  element type to match
	 * @return a {@link org.gradle.api.Transformer} instance to match the element of an iterable, never null.
	 */
	public static <E, T extends Iterable<? extends E>> TransformerUtils.Transformer<Iterable<E>, T> matching(Spec<? super E> spec) {
		if (SpecUtils.isSatisfyAll(spec)) {
			return NoOpTransformer.INSTANCE.withNarrowTypes();
		} else if (SpecUtils.isSatisfyNone(spec)) {
			return constant(emptyList());
		}
		return new MatchingTransformerAdapter<>(spec);
	}

	/** @see #matching(Spec) */
	@EqualsAndHashCode
	private static final class MatchingTransformerAdapter<E, T extends Iterable<? extends E>> implements TransformerUtils.Transformer<Iterable<E>, T> {
		private final Spec<? super E> spec;

		public MatchingTransformerAdapter(Spec<? super E> spec) {
			this.spec = requireNonNull(spec);
		}

		@Override
		public Iterable<E> transform(T elements) {
			val result = ImmutableList.<E>builder();
			for (E element : elements) {
				if (spec.isSatisfiedBy(element)) {
					result.add(element);
				}
			}
			return result.build();
		}

		@Override
		public String toString() {
			return "TransformerUtils.matching(" + spec + ")";
		}
	}

	@FunctionalInterface
	public interface Transformer<OUT, IN> extends org.gradle.api.Transformer<OUT, IN> {
		default <V> Transformer<OUT, V> compose(org.gradle.api.Transformer<? extends IN, ? super V> before) {
			return new ComposeTransformer<>(this, before);
		}

		default <V> Transformer<V, IN> andThen(org.gradle.api.Transformer<? extends V, ? super OUT> after) {
			return new ComposeTransformer<>(after, this);
		}
	}
}
