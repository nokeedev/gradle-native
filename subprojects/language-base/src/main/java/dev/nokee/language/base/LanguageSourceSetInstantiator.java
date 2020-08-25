package dev.nokee.language.base;

import dev.nokee.model.DomainObjectIdentifier;

import java.util.Set;

// TODO: Generalize to DomainObjectInstantiator & PolymorphicDomainObjectInstantiator???
public interface LanguageSourceSetInstantiator {
	<S extends LanguageSourceSet> S create(DomainObjectIdentifier identifier, Class<S> type);

	// TODO: Not sure about Set<? extends ...>
	Set<Class<? extends LanguageSourceSet>> getCreatableTypes();
}
