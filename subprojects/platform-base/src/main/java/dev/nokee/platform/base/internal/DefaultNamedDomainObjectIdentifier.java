package dev.nokee.platform.base.internal;

import lombok.Value;

@Value
public class DefaultNamedDomainObjectIdentifier implements NamedDomainObjectIdentifier {
	String name;
}
