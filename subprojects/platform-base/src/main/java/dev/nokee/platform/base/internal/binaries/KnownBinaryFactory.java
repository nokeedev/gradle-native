package dev.nokee.platform.base.internal.binaries;

import dev.nokee.model.internal.KnownDomainObject;
import dev.nokee.model.internal.KnownDomainObjectFactory;
import dev.nokee.model.internal.TypeAwareDomainObjectIdentifier;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.internal.BinaryIdentifier;

import javax.inject.Provider;

public final class KnownBinaryFactory implements KnownDomainObjectFactory<Binary> {
	private final Provider<BinaryRepository> repositoryProvider;
	private final Provider<BinaryConfigurer> configurerProvider;

	public KnownBinaryFactory(Provider<BinaryRepository> repositoryProvider, Provider<BinaryConfigurer> configurerProvider) {
		this.repositoryProvider = repositoryProvider;
		this.configurerProvider = configurerProvider;
	}

	public <T extends Binary> KnownBinary<T> create(BinaryIdentifier<T> identifier) {
		return new KnownBinary<>(identifier, repositoryProvider.get().identified(identifier), configurerProvider.get());
	}

	@Override
	public <S extends Binary> KnownDomainObject<S> create(TypeAwareDomainObjectIdentifier<S> identifier) {
		return create((BinaryIdentifier<S>)identifier);
	}
}
