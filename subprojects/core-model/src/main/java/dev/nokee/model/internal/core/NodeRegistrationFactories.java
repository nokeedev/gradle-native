package dev.nokee.model.internal.core;

import com.google.common.collect.ImmutableSet;
import dev.nokee.model.internal.type.ModelType;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class NodeRegistrationFactories implements NodeRegistrationFactoryRegistry, NodeRegistrationFactoryLookup {
	private final Map<ModelType<?>, NodeRegistrationFactory<?>> factories = new HashMap<>();

	@Override
	public <T> NodeRegistrationFactory<T> get(ModelType<T> type) {
		return (NodeRegistrationFactory<T>) factories.get(type);
	}

	@Override
	public <T> void registerFactory(ModelType<T> type, NodeRegistrationFactory<? extends T> factory) {
		factories.put(type, factory);
	}

	@Override
	public Set<ModelType<?>> getSupportedTypes() {
		return ImmutableSet.copyOf(factories.keySet());
	}
}
