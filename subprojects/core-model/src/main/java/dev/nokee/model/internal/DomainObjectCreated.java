package dev.nokee.model.internal;

import dev.nokee.model.DomainObjectIdentifier;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.Value;

@ToString
@EqualsAndHashCode
public class DomainObjectCreated<T> implements DomainObjectEvent {
	@Getter private final DomainObjectIdentifier identifier;
	@Getter private final T object;

	public DomainObjectCreated(TypeAwareDomainObjectIdentifier<?> identifier, T object) {
		assert identifier.getType().isInstance(object);
		this.identifier = identifier;
		this.object = object;
	}

	public DomainObjectCreated(DomainObjectIdentifier identifier, T object) {
		this.identifier = identifier;
		this.object = object;
	}
}
