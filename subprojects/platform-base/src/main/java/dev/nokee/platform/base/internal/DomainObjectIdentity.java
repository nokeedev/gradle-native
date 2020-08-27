package dev.nokee.platform.base.internal;

import dev.nokee.model.DomainObjectIdentifier;

public interface DomainObjectIdentity extends DomainObjectIdentifier {

	static DomainObjectIdentity named(String name) {
		return new DefaultNamedDomainObjectIdentity(name);
	}

	static DomainObjectIdentity of(BuildVariantInternal buildVariant) {
		return new BuildVariantDomainObjectIdentity(buildVariant);
	}
}
