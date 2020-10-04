package dev.nokee.platform.base.internal.variants;

import dev.nokee.model.internal.KnownDomainObjectFactory;
import dev.nokee.model.internal.TypeAwareDomainObjectIdentifier;
import dev.nokee.platform.base.Variant;
import dev.nokee.platform.base.internal.VariantIdentifier;

import javax.inject.Provider;

public final class KnownVariantFactory implements KnownDomainObjectFactory<Variant> {
	private final Provider<VariantRepository> repositoryProvider;
	private final Provider<VariantConfigurer> configurerProvider;

	public KnownVariantFactory(Provider<VariantRepository> repositoryProvider, Provider<VariantConfigurer> configurerProvider) {
		this.repositoryProvider = repositoryProvider;
		this.configurerProvider = configurerProvider;
	}

	public <T extends Variant> KnownVariant<T> create(VariantIdentifier<T> identifier) {
		return new KnownVariant<>(identifier, repositoryProvider.get().identified(identifier), configurerProvider.get());
	}

	@Override
	public <S extends Variant> KnownVariant<S> create(TypeAwareDomainObjectIdentifier<S> identifier) {
		return create((VariantIdentifier<S>)identifier);
	}
}
