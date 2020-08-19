package dev.nokee.platform.base.internal;

import lombok.Value;

@Value
public class BuildVariantDomainObjectIdentity implements NamedDomainObjectIdentity {
	BuildVariantInternal buildVariant;

	public BuildVariantDomainObjectIdentity(BuildVariantInternal buildVariant) {
		this.buildVariant = buildVariant;
	}

	@Override
	public String getName() {
		return BuildVariantNamer.INSTANCE.determineName(buildVariant);
	}
}
