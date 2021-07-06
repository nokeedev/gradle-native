package dev.nokee.model.core;

import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.provider.Provider;

public interface ModelProjection {

	// TODO: Not sure
	<T> boolean canBeViewedAs(Class<T> type);

	<T> T get(Class<T> type);

	<T> Provider<T> as(Class<T> type);

	ModelNode getOwner();

	<T> void whenRealized(Class<T> type, Action<? super T> action);

	interface Builder {
		Builder type(Class<?> type);

		Builder forProvider(NamedDomainObjectProvider<?> domainObjectProvider);

		Builder forInstance(Object instance);
	}
}
