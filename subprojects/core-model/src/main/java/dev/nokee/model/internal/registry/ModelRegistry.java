package dev.nokee.model.internal.registry;

import dev.nokee.model.internal.core.ModelIdentifier;
import dev.nokee.model.internal.core.ModelPath;
import dev.nokee.model.internal.core.ModelProvider;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.type.ModelType;

public interface ModelRegistry {
	default <T> ModelProvider<T> get(String path, Class<T> type) {
		return get(ModelIdentifier.of(ModelPath.path(path), ModelType.of(type)));
	}
	<T> ModelProvider<T> get(ModelIdentifier<T> identifier);

	<T> ModelProvider<T> register(ModelRegistration<T> registration);
}
