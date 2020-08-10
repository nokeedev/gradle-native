package dev.nokee.platform.base.internal;

import lombok.Value;

@Value
public class BuildVariantDomainObjectIdentity implements NamedDomainObjectIdentity {
	BuildVariant buildVariant;

	public BuildVariantDomainObjectIdentity(BuildVariant buildVariant) {
		this.buildVariant = buildVariant;
	}

	@Override
	public String getName() {
		return BuildVariantNamer.INSTANCE.determineName(buildVariant);
	}
}
