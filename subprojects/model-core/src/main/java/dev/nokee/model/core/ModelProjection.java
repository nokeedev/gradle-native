package dev.nokee.model.core;

import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.provider.Provider;

public interface ModelProjection {

	// TODO: Should probably use type instead
	<T> boolean canBeViewedAs(Class<T> type);

	<T> T get(Class<T> type); // TODO: Definitely not!

	<T> Provider<T> as(Class<T> type);

	/**
	 * Returns the owner of this projection.
	 *
	 * @return the owner of this projection, never null
	 */
	ModelNode getOwner();

	// TODO: It seems this may be overkill, maybe something more like whenRealized(Action<?> action)
	//   ... Maybe not... unsure
	<T> void whenRealized(Class<T> type, Action<? super T> action);

	/**
	 * Returns the registration type of this projection.
	 * The registration type is typically what is known as the public type.
	 * Since the projection is instantiated lazily, the type will never represent the implementation type.
	 *
	 * @return the projection type, never null
	 */
	Class<?> getType();

	void realize();

	interface Builder {
		<S> TypeAwareModelProjection.Builder<S> type(Class<S> type);

		<S> TypeAwareModelProjection.Builder<S> forProvider(NamedDomainObjectProvider<? extends S> domainObjectProvider);

		// TODO: If passing instance but no type and instance is a Gradle generated type, throw exception saying that you must specify a type (Gradle erase some generic information).
		<S> TypeAwareModelProjection.Builder<S> forInstance(S instance);
	}
}
