package dev.nokee.model.internal;

import dev.nokee.model.DomainObjectIdentifier;
import lombok.Value;

@Value
public class DomainObjectCreated<T> implements DomainObjectEvent {
	DomainObjectIdentifier identifier;
	T object;
}
