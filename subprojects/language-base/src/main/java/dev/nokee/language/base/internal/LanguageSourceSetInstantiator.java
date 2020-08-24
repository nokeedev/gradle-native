package dev.nokee.language.base.internal;

import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.model.DomainObjectIdentifier;

import java.util.Set;

public interface LanguageSourceSetInstantiator {
	<S extends LanguageSourceSet> S create(DomainObjectIdentifier identifier, Class<S> type);

	Set<Class<? extends LanguageSourceSet>> getCreatableTypes();
}
