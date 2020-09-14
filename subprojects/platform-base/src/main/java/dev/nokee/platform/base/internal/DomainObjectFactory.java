package dev.nokee.platform.base.internal;

public interface DomainObjectFactory<T> {
	T create();

	Class<T> getType();

	Class<? extends T> getImplementationType();

	DomainObjectIdentifier getIdentity();
}
