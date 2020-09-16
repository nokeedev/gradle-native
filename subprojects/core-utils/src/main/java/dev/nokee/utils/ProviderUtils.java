package dev.nokee.utils;

import com.google.common.collect.ImmutableList;
import lombok.EqualsAndHashCode;
import org.gradle.api.Transformer;
import org.gradle.api.internal.provider.DefaultProvider;
import org.gradle.api.internal.provider.ProviderInternal;
import org.gradle.api.internal.provider.Providers;
import org.gradle.api.provider.Provider;
import org.gradle.api.specs.Spec;
import org.gradle.api.specs.Specs;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.Callable;

import static dev.nokee.utils.TransformerUtils.constant;
import static dev.nokee.utils.TransformerUtils.toListTransformer;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;

public final class ProviderUtils {
	private ProviderUtils() {}

	/**
	 * Creates a Gradle {@link Provider} of the specified constant value without a {@link org.gradle.api.provider.ProviderFactory}.
	 *
	 * @param value a constant value to provide.
	 * @param <T> the type of the value.
	 * @return a {@link Provider} instance of the specified value.
	 */
	public static <T> Provider<T> fixed(T value) {
		return Providers.of(requireNonNull(value));
	}

	/**
	 * Creates a Gradle {@link Provider} provided by the specified callable without a {@link org.gradle.api.provider.ProviderFactory}.
	 *
	 * @param callable a value provider
	 * @param <T> the type of the value.
	 * @return a {@link Provider} instance of the specified callable provider.
	 */
	public static <T> Provider<T> supplied(Callable<T> callable) {
		return new DefaultProvider<>(requireNonNull(callable));
	}

	/**
	 * Returns the object type provided by the Gradle provider.
	 *
	 * @param self the provider to query the provided type.
	 * @param <T> the type of object provided
	 * @return a class representing the object type provided by the provider, or null if we can't figure it out.
	 */
	@Nullable
	public static <T> Class<T> getType(Provider<T> self) {
		requireNonNull(self);
		if (self instanceof ProviderInternal) {
			return ((ProviderInternal<T>) self).getType();
		}
		return null;
	}

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
