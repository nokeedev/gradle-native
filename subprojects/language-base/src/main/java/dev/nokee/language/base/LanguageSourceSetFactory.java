package dev.nokee.language.base;

import dev.nokee.model.DomainObjectIdentifier;

// TODO: Generalize to EntityFactory or DomainObjectFactory (would need to change the implementation)
public interface LanguageSourceSetFactory<T extends LanguageSourceSet> {
	T create(DomainObjectIdentifier identifier);
}
