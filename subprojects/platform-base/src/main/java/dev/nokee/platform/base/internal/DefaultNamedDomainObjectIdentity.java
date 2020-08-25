package dev.nokee.platform.base.internal;

import lombok.Value;

@Value
public class DefaultNamedDomainObjectIdentity implements NamedDomainObjectIdentity {
	String name;

	@Override
	public String toString() {
		return name;
	}
}
