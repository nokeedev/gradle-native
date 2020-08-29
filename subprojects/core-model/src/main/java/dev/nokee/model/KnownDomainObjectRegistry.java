package dev.nokee.model;

public interface KnownDomainObjectRegistry<T> {
	void add(KnownDomainObject<T> knowObject);

	KnownDomainObject<T> get(DomainObjectIdentifier identifier);
}
