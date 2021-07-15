package dev.nokee.model.core;

import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectProvider;

public interface TypeAwareModelProjection<T> extends ModelProjection {
	T get();
	void whenRealized(Action<? super T> action);

	interface Builder<T> {
		<S> Builder<S> type(Class<S> type);
		Builder<T> forProvider(NamedDomainObjectProvider<? extends T> provider);
		Builder<T> forInstance(T instance);
	}
}
