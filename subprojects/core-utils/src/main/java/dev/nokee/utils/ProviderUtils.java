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

import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.internal.provider.CollectionProviderInternal;
import org.gradle.api.internal.provider.DefaultProvider;
import org.gradle.api.internal.provider.ProviderInternal;
import org.gradle.api.internal.provider.Providers;
import org.gradle.api.provider.HasConfigurableValue;
import org.gradle.api.provider.HasMultipleValues;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ValueSourceParameters;
import org.gradle.api.provider.ValueSourceSpec;
import org.gradle.util.GradleVersion;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import java.util.function.Supplier;

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
	 * @param <T>  the type of object provided
	 * @return a {@link Provider} instance of the specified callable provider.
	 */
	public static <T> Provider<T> supplied(Callable<T> callable) {
		return new DefaultProvider<>(requireNonNull(callable));
	}

	/**
	 * Returned an undefined provider.
	 * It will always return null, throw an exception on get and returns false when querying for presence.
	 *
	 * @param <T>  the type of object provided
	 * @return a {@link Provider} instance without any value.
	 */
	public static <T> Provider<T> notDefined() {
		return Providers.notDefined();
	}

	/**
	 * Returns the object type provided by the Gradle provider.
	 *
	 * @param self the provider to query the provided type.
	 * @param <T>  the type of object provided
	 * @return a class representing the object type provided by the provider, or null if we can't figure it out.
	 */
	public static <T> Optional<Class<T>> getType(Provider<T> self) {
		requireNonNull(self);
		if (self instanceof ProviderInternal) {
			return Optional.ofNullable(((ProviderInternal<T>) self).getType());
		}
		return Optional.empty();
	}

	/**
	 * Returns the element type provided by the Gradle collection provider.
	 *
	 * @param self  the provider to query the provided element type, must not be null
	 * @param <T>  the type of element type provided
	 * @return a class representing the element type provided by the collection provider, or null if not a collection provider
	 */
	public static <T> Optional<Class<T>> getElementType(Provider<? extends Collection<T>> self) {
		requireNonNull(self);
		if (self instanceof CollectionProviderInternal) {
			@SuppressWarnings("unchecked")
			val elementType = (Class<T>) ((CollectionProviderInternal<T, ?>) self).getElementType();
			return Optional.of(elementType);
		}
		return Optional.empty();
	}

	public static <S> Provider<S> forUseAtConfigurationTime(Provider<S> provider) {
		if (GradleVersion.current().compareTo(GradleVersion.version("6.5")) >= 0 && GradleVersion.current().compareTo(GradleVersion.version("7.4")) < 0) {
			try {
				Method method = Provider.class.getMethod("forUseAtConfigurationTime");
				return Cast.uncheckedCast("using reflection to support newer Gradle", method.invoke(provider));
			} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
				throw new RuntimeException("Could not mark provider usage for configuration time because of an exception.", e);
			}
		}
		return provider;
	}

	/**
	 * Allows fluent call to {@link HasConfigurableValue#finalizeValue()}.
	 *
	 * @param value  the configurable value, must not be null
	 * @param <S>  the type of configurable value
	 * @return the specified configurable value, never null
	 */
	@SuppressWarnings("UnstableApiUsage")
	public static <S extends HasConfigurableValue> S finalizeValue(S value) {
		value.finalizeValue();
		return value;
	}

	/**
	 * Allows fluent call to {@link HasConfigurableValue#disallowChanges()}.
	 *
	 * @param value  the configurable value, must not be null
	 * @param <S>  the type of configurable value
	 * @return the specified configurable value, never null
	 */
	@SuppressWarnings("UnstableApiUsage")
	public static <S extends HasConfigurableValue> S disallowChanges(S value) {
		value.disallowChanges();
		return value;
	}

	/**
	 * If a value is present, invoke the specified action with the value, otherwise do nothing.
	 *
	 * @param self  the provider, must not be null
	 * @param action  the action to execute, must not be null
	 * @param <S>  the provider type
	 */
	public static <S> void ifPresent(Provider<S> self, Action<? super S> action) {
		Objects.requireNonNull(self);
		Objects.requireNonNull(action);
		final S value = self.getOrNull(); // Important to use this API as there is no guarantee the value stays the same
		if (value != null) {
			action.execute(value);
		}
	}

	/**
	 * If a value is present, performs the given action with the value, otherwise performs the given empty-based action.
	 *
	 * @param self  the provider, must not be null
	 * @param action  the action to be performed, if a value is present, must not be null
	 * @param emptyAction  the empty-based action to be performed, if no value is present, must not be null
	 * @param <S>  the provider type
	 */
	public static <S> void ifPresentOrElse(Provider<S> self, Action<? super S> action, Runnable emptyAction) {
		Objects.requireNonNull(self);
		Objects.requireNonNull(action);
		Objects.requireNonNull(emptyAction);
		final S value = self.getOrNull(); // Important to use this API as there is no guarantee the value stays the same
		if (value != null) {
			action.execute(value);
		} else {
			emptyAction.run();
		}
	}

	/**
	 * Allows fluent call to {@link HasConfigurableValue#finalizeValueOnRead()}.
	 *
	 * @param self  the configurable value, must not be null
	 * @param <S>  the type of configurable value
	 * @return the specified configurable value, never null
	 */
	public static <S extends HasConfigurableValue> S finalizeValueOnRead(S self) {
		self.finalizeValueOnRead();
		return self;
	}

	/**
	 * Resolves the provider by forcing the computation of a/its value.
	 * Note that since provider value are <b>not</b> memoized, using this API may result cause issues for called-sensitive value provider.
	 *
	 * @param self  the provider to resolve, must not be null
	 */
	public static void resolve(Provider<?> self) {
		self.getOrNull();
	}

	/**
	 * Returns a provider which value will be computed by combining the {@literal left} and {@literal right} provider values using the supplied combiner function.
	 *
	 * <p>If the supplied providers represents a task or the output of a task, the resulting provider
	 * will carry the dependency information.
	 *
	 * @param supplier  the accumulator supplier, must not be null
	 * @param left  the left provider to combine, must not be null
	 * @param right  the right provider to combine, must not be null
	 * @param combiner  the combiner function for both values, must not be null
	 * @return a provider which combine both provider values when resolved, never null
	 * @param <A>  a list provider to accumulate the left and right value, typically {@literal ListProperty<?>}
	 * @param <T>  the left provider type
	 * @param <U>  the right provider type
	 * @param <R>  the return provider type
	 */
	@SuppressWarnings("unchecked")
	public static <A extends Provider<? extends List<Object>> & HasMultipleValues<Object>, T, U, R> Provider<R> zip(Supplier<A> supplier, Provider<T> left, Provider<U> right, BiFunction<T, U, R> combiner) {
		val accumulator = supplier.get();
		accumulator.add(left);
		accumulator.add(right);
		return accumulator.map(it -> combiner.apply((T) it.get(0), (U) it.get(1)));
	}

	/**
	 * Returns an action that configures the {@link ValueSourceSpec} parameters with the specified action.
	 *
	 * <p><b>Note:</b> This utility method is strictly for convenience.
	 *
	 * @param action  an action that configure {@link ValueSourceParameters}, must not be null
	 * @return an action that configures the parameters, never null
	 * @param <P>  the value source parameters
	 */
	@SuppressWarnings("UnstableApiUsage")
	public static <P extends ValueSourceParameters> Action<ValueSourceSpec<P>> forParameters(Action<? super P> action) {
		return it -> it.parameters(action);
	}
}
