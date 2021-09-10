package dev.nokee.model.internal;

interface NamedDomainObjectRegistryInternal {
	<T> NamedDomainObjectContainerRegistry<T> registry(Class<T> type);
}
