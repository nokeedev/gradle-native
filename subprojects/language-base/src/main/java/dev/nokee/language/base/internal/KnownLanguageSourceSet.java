package dev.nokee.language.base.internal;

import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.model.internal.AbstractKnownDomainObject;
import dev.nokee.model.internal.DomainObjectConfigurer;
import org.gradle.api.provider.Provider;

public final class KnownLanguageSourceSet<T extends LanguageSourceSet> extends AbstractKnownDomainObject<LanguageSourceSet, T> {
	protected KnownLanguageSourceSet(LanguageSourceSetIdentifier<T> identifier, Provider<T> provider, DomainObjectConfigurer<LanguageSourceSet> configurer) {
		super(identifier, provider, configurer);
	}

	@Override
	@SuppressWarnings("unchecked")
	public LanguageSourceSetIdentifier<T> getIdentifier() {
		return (LanguageSourceSetIdentifier<T>) super.getIdentifier();
	}
}
