package dev.nokee.platform.base.internal;

public interface DomainObjectIdentity {

	static DomainObjectIdentity named(String name) {
		return new DefaultNamedDomainObjectIdentity(name);
	}

	static DomainObjectIdentity of(BuildVariant buildVariant) {
		return new BuildVariantDomainObjectIdentity(buildVariant);
	}
}
