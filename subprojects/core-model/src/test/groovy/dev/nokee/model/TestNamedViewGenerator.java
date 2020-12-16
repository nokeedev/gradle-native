package dev.nokee.model;

import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.model.internal.registry.ModelRegistry;

public interface TestNamedViewGenerator<T> extends TestViewGenerator<T> {
	NamedDomainObjectView<T> create(String name);

	Class<T> getElementType();

	<S extends T> Class<S> getSubElementType();

	ModelRegistry getModelRegistry();

	ModelLookup getModelLookup();
}
