package dev.nokee.model.internal;

import dev.nokee.model.DomainObjectIdentifier;
import lombok.Value;

@Value
public class RealizableDomainObjectDiscovered implements DomainObjectEvent {
	DomainObjectIdentifier identifier;
	RealizableDomainObject object;
}
