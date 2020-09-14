package dev.nokee.platform.base.internal;

import dev.nokee.model.internal.NamedDomainObjectIdentifier;
import lombok.Value;

@Value
public class BuildVariantDomainObjectIdentifier implements NamedDomainObjectIdentifier {
	BuildVariantInternal buildVariant;

	public BuildVariantDomainObjectIdentifier(BuildVariantInternal buildVariant) {
		this.buildVariant = buildVariant;
	}

	@Override
	public String getName() {
		return BuildVariantNamer.INSTANCE.determineName(buildVariant);
	}
}
