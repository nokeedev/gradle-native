package dev.nokee.model.internal.core;

import dev.nokee.model.DomainObjectProvider;

// TODO: Provide full test for mode provider
// TODO: Maybe consider using domain object provider directly
public interface ModelProvider<T> extends DomainObjectProvider<T> {
	ModelIdentifier<T> getIdentifier();

	T get();
}
