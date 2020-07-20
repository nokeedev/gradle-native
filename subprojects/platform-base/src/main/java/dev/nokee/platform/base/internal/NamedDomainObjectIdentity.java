package dev.nokee.platform.base.internal;

import lombok.Value;

@Value
public class NamedDomainObjectIdentity implements DomainObjectIdentity {
	String name;
}
