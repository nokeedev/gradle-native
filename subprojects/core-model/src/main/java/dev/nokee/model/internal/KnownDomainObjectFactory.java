package dev.nokee.model.internal;

public interface KnownDomainObjectFactory<T> {
	<S extends T> KnownDomainObject<S> create(TypeAwareDomainObjectIdentifier<S> identifier);
}
