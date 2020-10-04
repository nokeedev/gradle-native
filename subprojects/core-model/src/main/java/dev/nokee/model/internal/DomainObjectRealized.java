package dev.nokee.model.internal;

import dev.nokee.model.DomainObjectIdentifier;
import lombok.Value;

@Value
public class DomainObjectRealized<T> implements DomainObjectEvent {
	DomainObjectIdentifier identifier;
	T object;
}
