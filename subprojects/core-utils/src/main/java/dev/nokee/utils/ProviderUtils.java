package dev.nokee.utils;

import com.google.common.collect.ImmutableList;
import lombok.EqualsAndHashCode;
import org.gradle.api.Transformer;
import org.gradle.api.specs.Spec;
import org.gradle.api.specs.Specs;

import java.util.List;

import static dev.nokee.utils.TransformerUtils.constant;
import static dev.nokee.utils.TransformerUtils.toListTransformer;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;

public final class ProviderUtils {
	private ProviderUtils() {}

	/**
	 * Adapts a collection mapper to a Gradle collection provider transform.
	 * The result will apply a proper flatMap algorithm to the provided collection.
	 *
	 * @param mapper a flatMap mapper
	 * @param <OUT> output element type resulting from the flat map
	 * @param <IN> input element type to flat map
	 * @return a {@link Transformer} instance to flat map the element of Gradle collection provider, never null.
	 */
	public static <OUT, IN> Transformer<List<? extends OUT>, Iterable<? extends IN>> flatMap(Transformer<Iterable<? extends OUT>, ? super IN> mapper) {
		return new GradleCollectionProviderFlatMapAdapter<>(mapper);
	}

	/**
	 * Adapts a collection mapper to a Gradle collection provider transform.
	 * The result will apply a proper map algorithm to the provided collection.
	 *
	 * @param mapper a map mapper
	 * @param <OUT> output element type resulting from the map
	 * @param <IN> input element type to map
	 * @return a {@link Transformer} instance to map the element of Gradle collection provider, never null.
	 */
	public static <OUT, IN> Transformer<List<? extends OUT>, Iterable<? extends IN>> map(Transformer<? extends OUT, ? super IN> mapper) {
		return new GradleCollectionProviderMapAdapter<>(mapper);
	}

	/**
	 * Adapts a spec to a Gradle collection provider transform.
	 * The result will apply a filter algorithm to the provided collection.
	 *
	 * @param spec a filter spec
	 * @param <T> element type to filter
	 * @return a {@link Transformer} instance to filter the element of Gradle collection provider, never null.
	 */
	public static <T> Transformer<List<? extends T>, Iterable<? extends T>> filter(Spec<? super T> spec) {
		if (Specs.satisfyAll().equals(spec)) {
			return toListTransformer();
		} else if (Specs.satisfyNone().equals(spec)) {
			return constant(emptyList());
		}
		return new GradleCollectionProviderFilterAdapter<>(spec);
	}

	@EqualsAndHashCode
	private static final class GradleCollectionProviderFilterAdapter<T> implements Transformer<List<? extends T>, Iterable<? extends T>> {
		private final Spec<? super T> spec;

		public GradleCollectionProviderFilterAdapter(Spec<? super T> spec) {
			this.spec = requireNonNull(spec);
		}

		@Override
		public List<? extends T> transform(Iterable<? extends T> elements) {
			ImmutableList.Builder<T> result = ImmutableList.builder();
			for (T element : elements) {
				if (spec.isSatisfiedBy(element)) {
					result.add(element);
				}
			}
			return result.build();
		}

		@Override
		public String toString() {
			return "ProviderUtils.filter(" + spec + ")";
		}
	}

	@EqualsAndHashCode
	private static final class GradleCollectionProviderFlatMapAdapter<OUT, IN> implements Transformer<List<? extends OUT>, Iterable<? extends IN>> {
		private final Transformer<Iterable<? extends OUT>, ? super IN> mapper;

		public GradleCollectionProviderFlatMapAdapter(Transformer<Iterable<? extends OUT>, ? super IN> mapper) {
			this.mapper = requireNonNull(mapper);
		}

		@Override
		public List<? extends OUT> transform(Iterable<? extends IN> elements) {
			ImmutableList.Builder<OUT> result = ImmutableList.builder();
			for (IN element : elements) {
				result.addAll(mapper.transform(element));
			}
			return result.build();
		}

		@Override
		public String toString() {
			return "ProviderUtils.flatMap(" + mapper + ")";
		}
	}

	@EqualsAndHashCode
	private static final class GradleCollectionProviderMapAdapter<OUT, IN> implements Transformer<List<? extends OUT>, Iterable<? extends IN>> {
		private final Transformer<? extends OUT, ? super IN> mapper;

		public GradleCollectionProviderMapAdapter(Transformer<? extends OUT, ? super IN> mapper) {
			this.mapper = requireNonNull(mapper);
		}

		@Override
		public List<? extends OUT> transform(Iterable<? extends IN> elements) {
			ImmutableList.Builder<OUT> result = ImmutableList.builder();
			for (IN element : elements) {
				result.add(mapper.transform(element));
			}
			return result.build();
		}

		@Override
		public String toString() {
			return "ProviderUtils.map(" + mapper + ")";
		}
	}
}
