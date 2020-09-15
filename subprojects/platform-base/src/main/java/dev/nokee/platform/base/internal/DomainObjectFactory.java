package dev.nokee.platform.base.internal;

import dev.nokee.model.DomainObjectIdentifier;

public interface DomainObjectFactory<T> {
	T create();

	Class<T> getType();

	Class<? extends T> getImplementationType();

	DomainObjectIdentifier getIdentifier();
}
