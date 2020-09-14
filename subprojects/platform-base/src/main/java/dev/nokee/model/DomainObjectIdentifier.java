package dev.nokee.model;

import dev.nokee.platform.base.internal.BuildVariantDomainObjectIdentifier;
import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.model.internal.DefaultNamedDomainObjectIdentifier;

public interface DomainObjectIdentifier {

	static DomainObjectIdentifier named(String name) {
		return new DefaultNamedDomainObjectIdentifier(name);
	}

	static DomainObjectIdentifier of(BuildVariantInternal buildVariant) {
		return new BuildVariantDomainObjectIdentifier(buildVariant);
	}
}
