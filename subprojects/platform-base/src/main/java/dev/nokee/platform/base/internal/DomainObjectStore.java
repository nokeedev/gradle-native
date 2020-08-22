package dev.nokee.platform.base.internal;

import dev.nokee.platform.base.DomainObjectProvider;
import dev.nokee.platform.base.KnownDomainObject;
import org.gradle.api.Action;
import org.gradle.api.Transformer;
import org.gradle.api.provider.Provider;
import org.gradle.api.specs.Spec;

import java.util.List;

public interface DomainObjectStore {
	<U> DomainObjectProvider<U> register(DomainObjectFactory<U> factory);

	void whenElementKnown(Action<KnownDomainObject<?>> action);
	<U> void whenElementKnown(Class<U> type, Action<KnownDomainObject<? extends U>> action);

	void configureEach(Action<? super Object> action);
	<U> void configureEach(Class<U> type, Action<? super U> action);
	<U> void configureEach(Class<U> type, Spec<? super U> spec, Action<? super U> action);
	void configureEach(Spec<? super Object> spec, Action<? super Object> action);

	<S> Provider<List<? extends S>> flatMap(Transformer<Iterable<? extends S>, ? super Object> mapper);

	<S> Provider<List<? extends S>> select(Spec<? super S> spec);

	// TODO: Should not be here
	<T> void forceRealize(Class<T> publicType);
}
