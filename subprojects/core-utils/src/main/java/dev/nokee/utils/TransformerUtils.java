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
package dev.nokee.utils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import dev.nokee.util.internal.FlatTransformEachToCollectionAdapter;
import dev.nokee.util.internal.PeekTransformer;
import dev.nokee.util.internal.TransformEachToCollectionAdapter;
import dev.nokee.util.lambdas.SerializableTransformer;
import dev.nokee.util.lambdas.internal.SerializableTransformerAdapter;
import dev.nokee.utils.internal.WrappedTransformer;
import lombok.EqualsAndHashCode;
import lombok.val;
import org.gradle.api.provider.Provider;
import org.gradle.api.specs.Spec;
import org.gradle.internal.Transformers;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static dev.nokee.util.internal.GuavaImmutableCollectionBuilderFactories.listFactory;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;

public final class TransformerUtils {
	private TransformerUtils() {}

	/**
	 * Mechanism to create {@link org.gradle.api.Transformer} from Java lambdas that are {@link Serializable}.
	 *
	 * <p><b>Note:</b> The returned {@code Transformer} will provide an {@link Object#equals(Object)}/{@link Object#hashCode()} implementation based on the serialized bytes.
	 * The goal is to ensure the Java lambdas can be compared between each other after deserialization.
	 */
	public static <OUT, IN> org.gradle.api.Transformer<OUT, IN> ofSerializableTransformer(SerializableTransformer<OUT, IN> transformer) {
		return new SerializableTransformerAdapter<>(transformer);
	}

	public static <T> Transformer<T, T> peek(Consumer<? super T> action) {
		return ofTransformer(new PeekTransformer<>(action));
	}

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

	public static <OUT, IN extends OUT, T extends Iterable<IN>> Transformer<List<OUT>, T> toListTransformer() {
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
	private static final class ToListTransformerAdapter<OUT, IN, T extends Iterable<? extends IN>> implements Transformer<List<OUT>, T>, Serializable {
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

	public static <OutputElementType, InputElementType extends OutputElementType, InputType extends Iterable<InputElementType>> Transformer<Set<OutputElementType>, InputType> toSetTransformer() {
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
	private static final class ToSetTransformerAdapter<OUT, IN, T extends Iterable<? extends IN>> implements Transformer<Set<OUT>, T>, Serializable {
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

	/**
	 * Adapts an flat element mapper to transform each elements individually of the collection.
	 * The result will apply a proper flatMap algorithm to the provided collection.
	 *
	 * @param mapper  an element mapper
	 * @param <OutputElementType>  output element type resulting from the transform
	 * @param <InputElementType>  input element type to transform
	 * @return a {@link Transformer} instance to flat transform each the element of an iterable, never null.
	 */
	public static <OutputElementType, InputElementType> Transformer<Iterable<? extends OutputElementType>, Iterable<? extends InputElementType>> flatTransformEach(org.gradle.api.Transformer<? extends Iterable<OutputElementType>, InputElementType> mapper) {
		return ofTransformer(new FlatTransformEachToCollectionAdapter<>(listFactory(), mapper));
	}

	public static <ElementType> Transformer<Iterable<? extends ElementType>, Iterable<? extends Iterable<? extends ElementType>>> flatten() {
		return ofTransformer(it -> {
			final ImmutableList.Builder<ElementType> result = ImmutableList.builder();
			for (Iterable<? extends ElementType> elements : it) {
				result.addAll(elements);
			}
			return result.build();
		});
	}

	/**
	 * Adapts an element mapper to transform each elements individually of the collection.
	 * The result will apply a proper map algorithm to the provided collection.
	 *
	 * @param mapper  an element mapper
	 * @param <OutputElementType>  output element type resulting from the transform
	 * @param <InputElementType>  input element type to transform
	 * @return a {@link Transformer} instance to transform each the element of an iterable, never null.
	 */
	public static <OutputElementType, InputElementType> Transformer<Iterable<? extends OutputElementType>, Iterable<? extends InputElementType>> transformEach(org.gradle.api.Transformer<? extends OutputElementType, InputElementType> mapper) {
		if (isNoOpTransformer(mapper)) {
			return NoOpTransformer.INSTANCE.withNarrowTypes();
		}
		return ofTransformer(new TransformEachToCollectionAdapter<>(listFactory(), mapper));
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
	private static final class ComposeTransformer<A, B, C> implements Transformer<C, A>, Serializable {
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
	private static final class SupplierTransformer<T> implements Transformer<T, Object>, Serializable {
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
	 * @param <T>  input iterable type
	 * @param <E>  element type to match
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

	/**
	 * Keeps only instance of specified type.
	 * This approach differs from filtering elements as the returned elements will be of the correct type.
	 *
	 * @param type  the element type to filter, must not be null
	 * @param <OUT>  the outgoing element type
	 * @param <IN>  the incoming element type
	 * @return a transformer that will filter and cast the element based on the specified type, never null
	 */
	public static <OUT, IN> TransformerUtils.Transformer<Iterable<OUT>, IN> onlyInstanceOf(Class<OUT> type) {
		return new OnlyInstanceOfTransformer<>(type);
	}

	/** @see #onlyInstanceOf(Class) */
	@EqualsAndHashCode
	private static final class OnlyInstanceOfTransformer<OUT, IN> implements Transformer<Iterable<OUT>, IN> {
		private final Class<OUT> type;

		private OnlyInstanceOfTransformer(Class<OUT> type) {
			this.type = requireNonNull(type);
		}

		@Override
		public Iterable<OUT> transform(IN in) {
			if (type.isInstance(in)) {
				return ImmutableList.of(type.cast(in));
			} else {
				return ImmutableList.of();
			}
		}

		@Override
		public String toString() {
			return "TransformerUtils.onlyInstanceOf(" + type + ")";
		}
	}

	/**
	 * Keeps only instance of specified type transformed by the specified transformer.
	 * This approach differs from filtering elements as the returned elements will be of the correct type.
	 *
	 * @param type  the element type to filter, must not be null
	 * @param andThen  the transform to apply on each filtered elements, must not be null
	 * @param <OUT>  the outgoing element type
	 * @param <E>  the element type to keep and transform
	 * @param <IN>  the incoming element type
	 * @return a transformer that will filter, cast and transform the element based on the specified type, never null
	 */
	public static <OUT, E, IN> TransformerUtils.Transformer<Iterable<OUT>, IN> onlyInstanceOf(Class<E> type, org.gradle.api.Transformer<? extends OUT, ? super E> andThen) {
		return new OnlyInstanceOfAndThenTransformer<>(type, andThen);
	}

	/** @see #onlyInstanceOf(Class, org.gradle.api.Transformer) */
	@EqualsAndHashCode
	private static final class OnlyInstanceOfAndThenTransformer<OUT, E, IN> implements Transformer<Iterable<OUT>, IN> {
		private final Class<E> type;
		private final org.gradle.api.Transformer<? extends OUT, ? super E> andThen;

		private OnlyInstanceOfAndThenTransformer(Class<E> type, org.gradle.api.Transformer<? extends OUT, ? super E> andThen) {
			this.type = requireNonNull(type);
			this.andThen = requireNonNull(andThen);
		}

		@Override
		public Iterable<OUT> transform(IN in) {
			if (type.isInstance(in)) {
				return ImmutableList.of(andThen.transform(type.cast(in)));
			} else {
				return ImmutableList.of();
			}
		}

		@Override
		public String toString() {
			return "TransformerUtils.onlyInstanceOf(" + type + ", " + andThen + ")";
		}
	}

	/**
	 * Returns a transformer that will perform a {@link Stream#collect(Collector)} on the incoming iterable.
	 *
	 * @param collector  the collector to use on the stream, must not be null
	 * @param <OUT>  the transformer's output type
	 * @param <IN>  the transformer's input type
	 * @param <T>  the input's element type
	 * @return a transformer collecting the input element using a stream {@link Collector}, never null
	 */
	public static <OUT, IN extends Iterable<T>, T> Transformer<OUT, IN> collect(Collector<? super T, ?, OUT> collector) {
		return new CollectTransformer<>(collector);
	}

	/** @see #collect(Collector) */
	@EqualsAndHashCode
	private static final class CollectTransformer<OUT, IN extends Iterable<T>, T> implements Transformer<OUT, IN> {
		private final Collector<? super T, ?, OUT> collector;

		public CollectTransformer(Collector<? super T, ?, OUT> collector) {
			this.collector = requireNonNull(collector);
		}

		@Override
		public OUT transform(IN ts) {
			return StreamSupport.stream(ts.spliterator(), false).collect(collector);
		}

		@Override
		public String toString() {
			return "TransformerUtils.collect(" + collector + ")";
		}
	}

	/**
	 * Returns a transformer that will perform operation on the incoming iterable as a {@link Stream}.
	 *
	 * @param mapper  the stream mapper, must not be null
	 * @param <OUT>  the output type
	 * @param <IN>  the iterable input type
	 * @param <T>  the element type
	 * @return a iterable stream transfomrer, never null
	 */
	public static <OUT, IN extends Iterable<T>, T> Transformer<OUT, IN> stream(Function<? super Stream<T>, OUT> mapper) {
		return new JavaStreamTransformer<>(mapper);
	}

	/** @see #stream(Function)  */
	@EqualsAndHashCode
	private static final class JavaStreamTransformer<OUT, IN extends Iterable<T>, T> implements Transformer<OUT, IN> {
		private final Function<? super Stream<T>, OUT> mapper;

		private JavaStreamTransformer(Function<? super Stream<T>, OUT> mapper) {
			this.mapper = requireNonNull(mapper);
		}

		@Override
		public OUT transform(IN ts) {
			return mapper.apply(StreamSupport.stream(ts.spliterator(), false));
		}

		@Override
		public String toString() {
			return "TransformerUtils.stream(" + mapper + ")";
		}
	}

	public static <OUT> OUT nullSafeValue() {
		return null;
	}

	public static <OUT> Provider<OUT> nullSafeProvider() {
		return null;
	}

	public static <OUT, IN> Transformer<OUT, IN> ofTransformer(org.gradle.api.Transformer<? extends OUT, ? super IN> transformer) {
		return Transformer.of(transformer);
	}

	@FunctionalInterface
	public interface Transformer<OUT, IN> extends org.gradle.api.Transformer<OUT, IN> {
		static <OUT, IN> Transformer<OUT, IN> of(org.gradle.api.Transformer<? extends OUT, ? super IN> transformer) {
			requireNonNull(transformer);
			if (transformer instanceof Transformer) {
				@SuppressWarnings("unchecked")
				final Transformer<OUT, IN> result = (Transformer<OUT, IN>) transformer;
				return result;
			} else {
				return new WrappedTransformer<>(transformer);
			}
		}

		default <V> Transformer<OUT, V> compose(org.gradle.api.Transformer<? extends IN, ? super V> before) {
			return new ComposeTransformer<>(this, before);
		}

		default <V> Transformer<V, IN> andThen(org.gradle.api.Transformer<? extends V, ? super OUT> after) {
			return new ComposeTransformer<>(after, this);
		}
	}
}
