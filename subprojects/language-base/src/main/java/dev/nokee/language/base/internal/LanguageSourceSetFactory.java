package dev.nokee.language.base.internal;

import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.model.DomainObjectIdentifier;

public interface LanguageSourceSetFactory<T extends LanguageSourceSet> {
	T create(DomainObjectIdentifier identifier);
}
