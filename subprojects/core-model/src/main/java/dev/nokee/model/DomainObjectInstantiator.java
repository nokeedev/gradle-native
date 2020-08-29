package dev.nokee.model;

public interface DomainObjectInstantiator<T> {
	<S extends T> S newInstance(DomainObjectIdentifier identifier, Class<S> type);
}
