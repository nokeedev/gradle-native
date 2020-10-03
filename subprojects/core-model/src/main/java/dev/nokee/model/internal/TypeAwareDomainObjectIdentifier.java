package dev.nokee.model.internal;

import dev.nokee.model.DomainObjectIdentifier;

public interface TypeAwareDomainObjectIdentifier<T> extends DomainObjectIdentifier {
	Class<T> getType();
}
