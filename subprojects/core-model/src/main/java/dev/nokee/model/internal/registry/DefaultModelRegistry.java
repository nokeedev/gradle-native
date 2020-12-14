package dev.nokee.model.internal.registry;

import com.google.common.collect.ImmutableList;
import dev.nokee.internal.reflect.Instantiator;
import dev.nokee.model.DomainObjectProvider;
import dev.nokee.model.internal.core.*;
import lombok.val;

import java.util.*;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public final class DefaultModelRegistry implements ModelRegistry, ModelConfigurer, ModelLookup {
	private final Instantiator instantiator;
	private final Map<ModelPath, ModelNode> nodes = new LinkedHashMap<>();
	private final List<ModelConfiguration> configurations = new ArrayList<>();
	private final NodeStateListener nodeStateListener = new NodeStateListener();
	private final ModelNode rootNode;

	public DefaultModelRegistry(Instantiator instantiator) {
		this.instantiator = instantiator;
		rootNode = createRootNode().register();
		nodes.put(ModelPath.root(), rootNode);
	}

	private ModelNode createRootNode() {
		return ModelNode.builder()
			.withPath(ModelPath.root())
			.withConfigurer(this)
			.withLookup(this)
			.withListener(nodeStateListener)
			.withRegistry(this)
			.build();
	}

	@Override
	public <T> DomainObjectProvider<T> get(ModelIdentifier<T> identifier) {
		if (!nodes.containsKey(identifier.getPath())) {
			throw new IllegalStateException(String.format("Expected model node at '%s' but none was found", identifier.getPath()));
		}

		return new ModelNodeBackedProvider<>(identifier.getType(), get(identifier.getPath()));
	}

	@Override
	public <T> DomainObjectProvider<T> register(NodeRegistration<T> registration) {
		return rootNode.register(registration);
	}

	@Override
	public <T> DomainObjectProvider<T> register(ModelRegistration<T> registration) {
		// TODO: Should deny creating any model registration for root node
		if (!registration.getPath().getParent().isPresent() || !nodes.containsKey(registration.getPath().getParent().get())) {
			throw new IllegalArgumentException("Has to be direct descendant");
		}

		configurations.add(new ModelConfiguration(it -> it.getPath().equals(registration.getPath()), it -> {
			registration.getActions().forEach(action -> action.execute(it));
		}));
		val node = newNode(registration).register();
		return new ModelNodeBackedProvider<>(registration.getDefaultProjectionType(), node);
	}

	private ModelNode newNode(ModelRegistration<?> registration) {
		return ModelNode.builder()
			.withPath(registration.getPath())
			.withProjections(finalizeProjections(registration))
			.withConfigurer(this)
			.withListener(nodeStateListener)
			.withLookup(this)
			.withRegistry(this)
			.build();
	}

	private List<ModelProjection> finalizeProjections(ModelRegistration<?> registration) {
		return registration.getProjections().stream()
			.map(bindManagedProjectionWithInstantiator(instantiator)
				.andThen(decorateProjectionWithModelNode(() -> get(registration.getPath()))))
			.collect(Collectors.toList());
	}

	private static UnaryOperator<ModelProjection> bindManagedProjectionWithInstantiator(Instantiator instantiator) {
		return projection -> {
			if (projection instanceof ManagedModelProjection) {
				return ((ManagedModelProjection<?>) projection).bind(instantiator);
			}
			return projection;
		};
	}

	private static UnaryOperator<ModelProjection> decorateProjectionWithModelNode(Supplier<ModelNode> nodeSupplier) {
		return projection -> new ModelNodeDecoratingModelProjection(projection, nodeSupplier);
	}

	@Override
	public ModelNode get(ModelPath path) {
		Objects.requireNonNull(path);
		if (!nodes.containsKey(path)) {
			throw new IllegalArgumentException("Element at '" + path + "' wasn't found.");
		}
		return nodes.get(path);
	}

	@Override
	public Result query(ModelSpec spec) {
		val result = nodes.values().stream().filter(spec::isSatisfiedBy).collect(ImmutableList.toImmutableList());
		return new ModelLookupDefaultResult(result);
	}

	@Override
	public boolean has(ModelPath path) {
		return nodes.containsKey(Objects.requireNonNull(path));
	}

	@Override
	public void configureMatching(ModelSpec spec, ModelAction action) {
		val configuration = new ModelConfiguration(spec, action);
		for (val node : nodes.values()) {
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
			for (int i = 0; i < configurations.size(); ++i) {
				val configuration = configurations.get(i);
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
