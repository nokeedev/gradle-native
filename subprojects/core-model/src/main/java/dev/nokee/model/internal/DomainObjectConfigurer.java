package dev.nokee.model.internal;

import dev.nokee.model.DomainObjectIdentifier;
import org.gradle.api.Action;

public interface DomainObjectConfigurer<T> {
	<S extends T> void configureEach(DomainObjectIdentifier owner, Class<S> type, Action<? super S> action);
	<S extends T> void configure(TypeAwareDomainObjectIdentifier<S> identifier, Action<? super S> action);
	<S extends T> void whenElementKnown(DomainObjectIdentifier owner, Class<S> type, Action<? super TypeAwareDomainObjectIdentifier<S>> action);
}
