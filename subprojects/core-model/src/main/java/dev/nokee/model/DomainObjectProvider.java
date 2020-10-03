package dev.nokee.model;

import org.gradle.api.Action;
import org.gradle.api.Transformer;
import org.gradle.api.provider.Provider;

public interface DomainObjectProvider<T> {
	DomainObjectIdentifier getIdentifier();

	Class<T> getType();

	void configure(Action<? super T> action);

	T get();

	<S> Provider<S> map(Transformer<? extends S, ? super T> transformer);

	<S> Provider<S> flatMap(Transformer<? extends Provider<? extends S>, ? super T> transformer);
}
