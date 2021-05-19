package dev.nokee.utils;

import org.gradle.api.internal.provider.DefaultProvider;
import org.gradle.api.internal.provider.ProviderInternal;
import org.gradle.api.internal.provider.Providers;
import org.gradle.api.provider.Provider;
import org.gradle.util.GradleVersion;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.concurrent.Callable;

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
	public static <T> Optional<Class<T>> getType(Provider<T> self) {
		requireNonNull(self);
		if (self instanceof ProviderInternal) {
			return Optional.ofNullable(((ProviderInternal<T>) self).getType());
		}
		return Optional.empty();
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
