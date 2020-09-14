package dev.nokee.platform.base.internal;

public interface DomainObjectIdentifier {

	static DomainObjectIdentifier named(String name) {
		return new DefaultNamedDomainObjectIdentifier(name);
	}

	static DomainObjectIdentifier of(BuildVariantInternal buildVariant) {
		return new BuildVariantDomainObjectIdentifier(buildVariant);
	}
}
