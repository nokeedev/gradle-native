package dev.nokee.platform.base.internal;

public interface DomainObjectIdentity {

	static DomainObjectIdentity named(String name) {
		return new NamedDomainObjectIdentity(name);
	}
}
