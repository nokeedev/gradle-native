package dev.nokee.model.internal.registry;

import com.google.common.collect.ImmutableList;
import dev.nokee.model.internal.core.*;
import lombok.val;
import org.gradle.api.model.ObjectFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public final class DefaultModelRegistry implements ModelRegistry {
	private final ObjectFactory objectFactory;
	private final Map<ModelPath, ModelNode> nodes = new HashMap<>();

	public DefaultModelRegistry(ObjectFactory objectFactory) {
		this.objectFactory = objectFactory;
		nodes.put(ModelPath.root(), new ModelNode(ModelPath.root()));
	}

	@Override
	public <T> ModelProvider<T> get(ModelIdentifier<T> identifier) {
		if (!nodes.containsKey(identifier.getPath())) {
			throw new IllegalStateException(String.format("Expected model node at '%s' but none was found", identifier.getPath()));
		}

		return new ModelNodeBackedProvider<>(identifier.getType(), nodes.get(identifier.getPath()));
	}

	@Override
	public <T> ModelProvider<T> register(ModelRegistration<T> registration) {
		// TODO: Should deny creating any model registration for root node
		if (!registration.getPath().getParent().isPresent() || !nodes.containsKey(registration.getPath().getParent().get())) {
			throw new IllegalArgumentException("Has to be direct descendant");
		}

		registration = decorateProjectionWithModelNode(defaultManagedProjection(registration));
		val node = new ModelNode(registration.getPath(), registration.getProjections());
		nodes.put(registration.getPath(), node);
		return new ModelNodeBackedProvider<>(registration.getType(), node);
	}

	private <T> ModelRegistration<T> defaultManagedProjection(ModelRegistration<T> registration) {
		if (registration.getProjections().isEmpty()) {
			return registration.withProjections(ImmutableList.of(new MemoizedModelProjection(UnmanagedCreatingModelProjection.of(registration.getType(), () -> objectFactory.newInstance(registration.getType().getConcreteType())))));
		}
		return registration;
	}

	private <T> ModelRegistration<T> decorateProjectionWithModelNode(ModelRegistration<T> registration) {
		return registration.withProjections(registration.getProjections().stream().map(projection -> new ModelNodeDecoratingModelProjection(projection, () -> nodes.get(registration.getPath()))).collect(Collectors.toList()));
	}
}
