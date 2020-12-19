package dev.nokee.model.internal.registry;

import dev.nokee.model.DomainObjectProvider;
import dev.nokee.model.internal.core.*;
import dev.nokee.model.internal.type.ModelType;

public interface ModelRegistry {
	default <T> DomainObjectProvider<T> get(String path, Class<T> type) {
		return get(ModelIdentifier.of(ModelPath.path(path), ModelType.of(type)));
	}
	<T> DomainObjectProvider<T> get(ModelIdentifier<T> identifier);

	<T> DomainObjectProvider<T> register(NodeRegistration<T> registration);
	<T> DomainObjectProvider<T> register(ModelRegistration<T> registration);
}
