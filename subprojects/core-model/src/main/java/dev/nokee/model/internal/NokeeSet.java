package dev.nokee.model.internal;

import org.gradle.api.provider.Provider;
import org.gradle.api.specs.Spec;

import java.util.Set;

public interface NokeeSet<T> extends NokeeCollection<T> {
	/**
	 * Realize all values to their value.
	 *
	 * @return a set of values.
	 */
	Set<T> get();

	/**
	 * Returns a Gradle provider of the values in this set.
	 * The provider is live and reflect any changes to this set.
	 *
	 * @return a Gradle provider, never null.
	 */
	Provider<? extends Set<T>> getElements();

	/**
	 * Filter the element of this set without resolving them.
	 *
	 * @param spec a spec for each element to satisfy.
	 * @return a live filtered set, never null.
	 */
	NokeeSet<T> filter(Spec<? super T> spec);
}
