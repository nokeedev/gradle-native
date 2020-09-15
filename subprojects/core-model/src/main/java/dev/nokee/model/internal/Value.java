package dev.nokee.model.internal;

import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Transformer;
import org.gradle.api.provider.Provider;

import java.util.function.Supplier;

/**
 * A value computed lazily.
 * It is similar to the Gradle {@link Provider} but does differ in it's goal.
 * This value is never null and offer mapInPlace capability absent from Gradle Provider API.
 *
 * @param <T> the type of the value.
 */
public interface Value<T> {
	/**
	 * Returns this value after all in-place mapper were applied.
	 *
	 * @return this value, never null.
	 */
	T get();

	/**
	 * Returns the type of this value.
	 *
	 * @return the type of this value.
	 */
	Class<T> getType();

	/**
	 * Map the value as a Gradle {@link Provider}.
	 * The provider is live and will reflect any changes to this value.
	 *
	 * @param mapper a mapper transform
	 * @param <S> the mapped value type
	 * @return a {@link Provider} instance of this mapped value, never null.
	 */
	<S> Provider<S> map(Transformer<? extends S, ? super T> mapper);

	/**
	 * Flat map the value as a Gradle {@link Provider}.
	 * The provider is live and will reflect any changes to this value.
	 *
	 * @param mapper a flat mapper transform
	 * @param <S> the flat mapped value type
	 * @return a {@link Provider} instance of this flat mapped value, never null.
	 */
	<S> Provider<S> flatMap(Transformer<? extends Provider<? extends S>, ? super T> mapper);

	/**
	 * Maps the value in-place by replacing cached instance with the mapped instance.
	 *
	 * @param mapper a mapper transform.
	 * @return this value instance, never null.
	 */
	Value<T> mapInPlace(Transformer<? extends T, ? super T> mapper);

	/**
	 * Returns a Gradle {@link Provider} instance of this value.
	 * The provider is live and will reflect any in-place mapping upon realization.
	 *
	 * @return a {@link Provider} instance of this value, never null.
	 */
	Provider<T> toProvider();

	/**
	 * Creates a fixed (constant) value.
	 *
	 * @param value the initial value.
	 * @param <T> the type of the value.
	 * @return a value of the specified constant, never null.
	 */
	static <T> Value<T> fixed(T value) {
		return new ValueFixedImpl<>(value);
	}

	/**
	 * Creates a value provided by a Gradle {@link NamedDomainObjectProvider}.
	 *
	 * @param provider the provider of the value
	 * @param <T> the type of the value.
	 * @return a value of the specified provider, never null.
	 */
	static <T> Value<T> provided(NamedDomainObjectProvider<T> provider) {
		return new ValueProvidedImpl<>(provider);
	}

	/**
	 * Creates a value supplied by a {@link Supplier}.
	 *
	 * @param type the object type supplied.
	 * @param supplier the supplier of the value.
	 * @param <T> the type of the value.
	 * @return a value of the specified supplier, never null.
	 */
	static <T> Value<T> supplied(Class<T> type, Supplier<T> supplier) {
		return new ValueSuppliedImpl<>(type, supplier);
	}
}
