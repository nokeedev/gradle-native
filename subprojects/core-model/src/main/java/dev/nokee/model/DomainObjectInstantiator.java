package dev.nokee.model;

import dev.nokee.model.DomainObjectIdentifier;

public interface DomainObjectInstantiator<T> {
	<S extends T> S newInstance(DomainObjectIdentifier identifier, Class<S> type);
}
