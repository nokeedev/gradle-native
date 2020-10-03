package dev.nokee.model.internal;

import lombok.Value;

@Value
public class DomainObjectDiscovered<T> implements DomainObjectEvent {
	TypeAwareDomainObjectIdentifier<? extends T> identifier;

	public Class<?> getType() {
		return identifier.getType();
	}
}
