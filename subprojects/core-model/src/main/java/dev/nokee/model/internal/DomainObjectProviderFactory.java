package dev.nokee.model.internal;

import dev.nokee.model.DomainObjectProvider;

public interface DomainObjectProviderFactory<T> {
	<S extends T> DomainObjectProvider<S> create(TypeAwareDomainObjectIdentifier<S> identifier);
}
