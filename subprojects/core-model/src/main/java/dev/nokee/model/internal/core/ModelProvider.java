package dev.nokee.model.internal.core;

public interface ModelProvider<T> {
	ModelIdentifier<T> getIdentifier();

	T get();
}
