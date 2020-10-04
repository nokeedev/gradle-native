package dev.nokee.platform.base.internal.variants;

import dev.nokee.model.internal.AbstractKnownDomainObject;
import dev.nokee.platform.base.Variant;
import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.platform.base.internal.VariantIdentifier;
import org.gradle.api.provider.Provider;

public final class KnownVariant<T extends Variant> extends AbstractKnownDomainObject<Variant, T> {
	private final VariantIdentifier<T> identifier;

	public KnownVariant(VariantIdentifier<T> identifier, Provider<T> provider, VariantConfigurer configurer) {
		super(identifier, provider, configurer);
		this.identifier = identifier;
	}

	@Override
	public VariantIdentifier<T> getIdentifier() {
		return identifier;
	}

	public BuildVariantInternal getBuildVariant() {
		return (BuildVariantInternal) identifier.getBuildVariant();
	}
}
