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
import org.gradle.util.GradleVersion;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
	 * Returned an undefined provider.
	 * It will always return null, throw an exception on get and returns false when querying for presence.
	 *
	 * @return a {@link Provider} instance without any value.
	 */
	public static <T> Provider<T> notDefined() {
		return Providers.notDefined();
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
	 * Adapts a spec to a Gradle collection provider transform.
	 * The result will apply a filter algorithm to the provided collection.
	 *
	 * @param spec a filter spec
	 * @param <T> element type to filter
	 * @return a {@link Transformer} instance to filter the element of Gradle collection provider, never null.
	 */
	public static <T> Transformer<List<T>, Iterable<T>> filter(Spec<? super T> spec) {
		if (Specs.satisfyAll().equals(spec)) {
			return toListTransformer();
		} else if (Specs.satisfyNone().equals(spec)) {
			return constant(emptyList());
		}
		return new GradleCollectionProviderFilterAdapter<>(spec);
	}

	@EqualsAndHashCode
	private static final class GradleCollectionProviderFilterAdapter<T> implements Transformer<List<T>, Iterable<T>> {
		private final Spec<? super T> spec;

		public GradleCollectionProviderFilterAdapter(Spec<? super T> spec) {
			this.spec = requireNonNull(spec);
		}

		@Override
		public List<T> transform(Iterable<T> elements) {
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

	public static <S> Provider<S> forUseAtConfigurationTime(Provider<S> provider) {
		if (GradleVersion.current().compareTo(GradleVersion.version("6.5")) >= 0) {
			try {
				Method method = Provider.class.getMethod("forUseAtConfigurationTime");
				return Cast.uncheckedCast("using reflection to support newer Gradle", method.invoke(provider));
			} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
				throw new RuntimeException("Could not mark provider usage for configuration time because of an exception.", e);
			}
		}
		return provider;
	}
}
