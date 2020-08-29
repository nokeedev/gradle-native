package dev.nokee.model;

import org.gradle.api.Action;
import org.gradle.api.Transformer;
import org.gradle.api.provider.Provider;

public interface KnownDomainObject<T> {
	void configure(Action<? super T> action);

	Class<T> getType();

	DomainObjectIdentifier getIdentity();

	<S> Provider<S> map(Transformer<? extends S, ? super T> transformer);

	<S> Provider<S> flatMap(Transformer<? extends Provider<? extends S>, ? super T> transformer);
}
