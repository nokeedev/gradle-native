package dev.nokee.model.internal;

import dev.nokee.model.DomainObjectIdentifier;

public interface KnownDomainObject<T> {
	DomainObjectIdentifier getIdentifier();
	Class<T> getType();
}
