package dev.nokee.model.internal.core;

import dev.nokee.model.internal.type.ModelType;

import java.util.Set;

public interface NodeRegistrationFactoryLookup {
	<T> NodeRegistrationFactory<T> get(ModelType<T> type);

	Set<ModelType<?>> getSupportedTypes();
}
