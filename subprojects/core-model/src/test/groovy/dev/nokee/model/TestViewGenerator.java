package dev.nokee.model;

import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.model.internal.registry.ModelRegistry;

public interface TestViewGenerator<T> {
	DomainObjectView<T> create(String name);

	Class<? extends DomainObjectView<T>> getSubjectType();

	Class<T> getElementType();

	<S extends T> Class<S> getSubElementType();

	ModelRegistry getModelRegistry();

	ModelLookup getModelLookup();
}
