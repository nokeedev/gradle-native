package dev.nokee.model.internal;

import dev.nokee.model.DomainObjectIdentifier;

public interface NameAwareDomainObjectIdentifier extends DomainObjectIdentifier {
	Object getName();
}
