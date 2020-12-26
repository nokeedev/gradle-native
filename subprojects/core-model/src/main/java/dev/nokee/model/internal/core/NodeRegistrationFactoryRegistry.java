package dev.nokee.model.internal.core;

import dev.nokee.model.internal.type.ModelType;

public interface NodeRegistrationFactoryRegistry {
	<T> void registerFactory(ModelType<T> type, NodeRegistrationFactory<? extends T> factory);
}
