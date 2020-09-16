package dev.nokee.model.internal;

import org.gradle.api.Action;
import org.gradle.api.provider.Provider;
import org.gradle.api.specs.Spec;

import java.util.Collection;

/**
 * Represents a lazy and live collection of values.
 *
 * @param <T> the type of the values in this collection.
 */
public interface NokeeCollection<T> {
	/**
	 * Add a lazy value to this collection.
	 *
	 * @param value a lazy value to add to this collection.
	 */
	void add(Value<? extends T> value);

	/**
	 * Returns the number of element in this collection.
	 *
	 * @return the number of elements in this collection.
	 */
	int size();

	/**
	 * Register an action to execute for each element (past and future) of this collection.
	 *
	 * @param action The action to be performed for each element
	 * @throws NullPointerException if the specified action is null
	 */
	void forEach(Action<? super T> action);

	/**
	 * Realize all values to their value.
	 *
	 * @return a collection of values.
	 */
	Collection<? extends T> get();

	/**
	 * Returns a Gradle provider of the values in this collection.
	 * The provider is live and reflect any changes to this collection.
	 *
	 * @return a Gradle provider, never null.
	 */
	Provider<Collection<? extends T>> getElements();

	/**
	 * Filter the element of this collection without resolving them.
	 *
	 * @param spec a spec for each element to satisfy.
	 * @param <S> the new type of the element
	 * @return a live filtered collection, never null.
	 */
	<S extends T> NokeeCollection<S> filter(Spec<? super S> spec);
}
