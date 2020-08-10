package dev.nokee.platform.base.internal;

import lombok.Value;

@Value
public class DefaultNamedDomainObjectIdentity implements NamedDomainObjectIdentity {
	String name;
}
