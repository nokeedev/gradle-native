package dev.nokee.language.base.internal;

import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.model.internal.KnownDomainObject;
import dev.nokee.model.internal.KnownDomainObjectFactory;
import dev.nokee.model.internal.TypeAwareDomainObjectIdentifier;

import javax.inject.Provider;

public final class KnownLanguageSourceSetFactory implements KnownDomainObjectFactory<LanguageSourceSet> {
	private final Provider<LanguageSourceSetRepository> repositoryProvider;
	private final Provider<LanguageSourceSetConfigurer> configurerProvider;

	public KnownLanguageSourceSetFactory(Provider<LanguageSourceSetRepository> repositoryProvider, Provider<LanguageSourceSetConfigurer> configurerProvider) {
		this.repositoryProvider = repositoryProvider;
		this.configurerProvider = configurerProvider;
	}

	public <T extends LanguageSourceSet> KnownLanguageSourceSet<T> create(LanguageSourceSetIdentifier<T> identifier) {
		return new KnownLanguageSourceSet<>(identifier, repositoryProvider.get().identified(identifier), configurerProvider.get());
	}

	@Override
	public <S extends LanguageSourceSet> KnownDomainObject<S> create(TypeAwareDomainObjectIdentifier<S> identifier) {
		return create((LanguageSourceSetIdentifier<S>)identifier);
	}
}
