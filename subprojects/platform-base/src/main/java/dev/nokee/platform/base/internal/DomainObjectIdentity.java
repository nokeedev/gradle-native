package dev.nokee.platform.base.internal;

// Rename to identifier
public interface DomainObjectIdentity {

	static DomainObjectIdentity named(String name) {
		return new DefaultNamedDomainObjectIdentity(name);
	}

	static DomainObjectIdentity of(BuildVariantInternal buildVariant) {
		return new BuildVariantDomainObjectIdentity(buildVariant);
	}
}
