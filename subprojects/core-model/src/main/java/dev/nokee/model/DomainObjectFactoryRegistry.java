package dev.nokee.model;

public interface DomainObjectFactoryRegistry<T> {
    <U extends T> void registerFactory(Class<U> type, DomainObjectFactory<? extends U> factory);

	<U extends T> void registerBinding(Class<U> type, Class<? extends U> implementationType);

	<U extends T> void registerFactoryIfAbsent(Class<U> type, DomainObjectFactory<? extends U> factory);
}
