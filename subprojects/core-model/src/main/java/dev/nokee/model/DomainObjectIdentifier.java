package dev.nokee.model;

import dev.nokee.model.internal.NamedDomainObjectIdentifierImpl;

public interface DomainObjectIdentifier {
	static DomainObjectIdentifier named(String name) {
		return new NamedDomainObjectIdentifierImpl(name);
	}
}
