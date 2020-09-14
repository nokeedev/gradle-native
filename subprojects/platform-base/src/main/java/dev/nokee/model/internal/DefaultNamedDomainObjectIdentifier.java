package dev.nokee.model.internal;

import lombok.Value;

@Value
public class DefaultNamedDomainObjectIdentifier implements NamedDomainObjectIdentifier {
	String name;
}
