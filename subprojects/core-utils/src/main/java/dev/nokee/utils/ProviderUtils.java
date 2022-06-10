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
import org.gradle.api.provider.Provider;
import org.gradle.util.GradleVersion;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Objects;
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
		final S value = self.getOrNull();
		if (value != null) {
			action.execute(value);
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
}
