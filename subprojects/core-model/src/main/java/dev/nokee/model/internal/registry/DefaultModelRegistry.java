package dev.nokee.model.internal.registry;

import com.google.common.collect.ImmutableList;
import dev.nokee.model.internal.core.*;
import lombok.val;
import org.gradle.api.model.ObjectFactory;

import java.util.*;
import java.util.stream.Collectors;

public final class DefaultModelRegistry implements ModelRegistry, ModelConfigurer, ModelLookup {
	private final ObjectFactory objectFactory;
	private final Map<ModelPath, ModelNode> nodes = new LinkedHashMap<>();
	private final List<ModelConfiguration> configurations = new ArrayList<>();
	private final NodeStateListener nodeStateListener = new NodeStateListener();

	public DefaultModelRegistry(ObjectFactory objectFactory) {
		this.objectFactory = objectFactory;
		nodes.put(ModelPath.root(), createRootNode().register());
	}

	private ModelNode createRootNode() {
		return ModelNode.builder()
			.withPath(ModelPath.root())
			.withConfigurer(this)
			.withLookup(this)
			.withListener(nodeStateListener)
			.build();
	}

	@Override
	public <T> ModelProvider<T> get(ModelIdentifier<T> identifier) {
		if (!nodes.containsKey(identifier.getPath())) {
			throw new IllegalStateException(String.format("Expected model node at '%s' but none was found", identifier.getPath()));
		}

		return new ModelNodeBackedProvider<>(identifier.getType(), get(identifier.getPath()));
	}

	@Override
	public <T> ModelProvider<T> register(ModelRegistration<T> registration) {
		// TODO: Should deny creating any model registration for root node
		if (!registration.getPath().getParent().isPresent() || !nodes.containsKey(registration.getPath().getParent().get())) {
			throw new IllegalArgumentException("Has to be direct descendant");
		}

		val node = newNode(registration).register();
		return new ModelNodeBackedProvider<>(registration.getType(), node);
	}

	private ModelNode newNode(ModelRegistration<?> registration) {
		registration = decorateProjectionWithModelNode(defaultManagedProjection(registration));
		return ModelNode.builder()
			.withPath(registration.getPath())
			.withProjections(registration.getProjections())
			.withConfigurer(this)
			.withListener(nodeStateListener)
			.withLookup(this)
			.build();
	}

	private <T> ModelRegistration<T> defaultManagedProjection(ModelRegistration<T> registration) {
		if (registration.getProjections().isEmpty()) {
			return registration.withProjections(ImmutableList.of(new MemoizedModelProjection(UnmanagedCreatingModelProjection.of(registration.getType(), () -> objectFactory.newInstance(registration.getType().getConcreteType())))));
		}
		return registration;
	}

	private <T> ModelRegistration<T> decorateProjectionWithModelNode(ModelRegistration<T> registration) {
		return registration.withProjections(registration.getProjections().stream().map(projection -> new ModelNodeDecoratingModelProjection(projection, () -> get(registration.getPath()))).collect(Collectors.toList()));
	}

	@Override
	public ModelNode get(ModelPath path) {
		Objects.requireNonNull(path);
		if (!nodes.containsKey(path)) {
			throw new IllegalArgumentException();
		}
		return nodes.get(path);
	}

	@Override
	public Result query(ModelSpec spec) {
		val result = nodes.values().stream().filter(spec::isSatisfiedBy).collect(ImmutableList.toImmutableList());
		return new Result() {
			@Override
			public List<ModelNode> get() {
				return result;
			}
		};
	}

	@Override
	public void configureMatching(ModelSpec spec, ModelAction action) {
		val configuration = new ModelConfiguration(spec, action);
		for(val node : nodes.values()) {
			configuration.notifyFor(node);
		}
		configurations.add(configuration);
	}

	private final class NodeStateListener implements ModelNodeListener {
		@Override
		public void initialized(ModelNode node) {
			notify(node);
		}

		@Override
		public void registered(ModelNode node) {
			nodes.put(node.getPath(), node);
			notify(node);
		}

		@Override
		public void realized(ModelNode node) {
			notify(node);
		}

		private void notify(ModelNode node) {
			for (val configuration : configurations) {
				configuration.notifyFor(node);
			}
		}
	}

	private static final class ModelConfiguration {
		private final ModelSpec spec;
		private final ModelAction action;

		private ModelConfiguration(ModelSpec spec, ModelAction action) {
			this.spec = Objects.requireNonNull(spec);
			this.action = Objects.requireNonNull(action);
		}

		public void notifyFor(ModelNode node) {
			if (spec.isSatisfiedBy(node)) {
				action.execute(node);
			}
		}
	}
}
