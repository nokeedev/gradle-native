package dev.nokee.model.internal;

import dev.nokee.model.DomainObjectIdentifier;

import java.util.Optional;

public interface DomainObjectIdentifierInternal extends DomainObjectIdentifier {
	Optional<? extends DomainObjectIdentifierInternal> getParentIdentifier();

	String getDisplayName();
}
