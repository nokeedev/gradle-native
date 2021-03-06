package dev.nokee.model.internal.registry;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import dev.nokee.internal.reflect.Instantiator;
import dev.nokee.model.DomainObjectProvider;
import dev.nokee.model.internal.core.*;
import lombok.val;

import java.util.*;

public final class DefaultModelRegistry implements ModelRegistry, ModelConfigurer, ModelLookup {
	private final Instantiator instantiator;
	private final Map<ModelPath, ModelNode> nodes = new LinkedHashMap<>();
	private final List<ModelAction> configurations = new ArrayList<>();
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
			.withInstantiator(instantiator)
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

		registration.getActions().forEach(configurations::add);
		val node = newNode(registration).register();
		return new ModelNodeBackedProvider<>(registration.getDefaultProjectionType(), node);
	}

	private ModelNode newNode(ModelRegistration<?> registration) {
		return ModelNode.builder()
			.withPath(registration.getPath())
			.withConfigurer(this)
			.withListener(nodeStateListener)
			.withLookup(this)
			.withRegistry(this)
			.withInstantiator(instantiator)
			.build();
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
	public boolean anyMatch(ModelSpec spec) {
		return nodes.values().stream().anyMatch(spec::isSatisfiedBy);
	}

	@Override
	public void configure(ModelAction configuration) {
		configurations.add(configuration);
		val size = nodes.size();
		for (int i = 0; i < size; i++) {
			configuration.execute(Iterables.get(nodes.values(), i));
		}
	}

	private final class NodeStateListener implements ModelNodeListener {
		@Override
		public void created(ModelNode node) {
			notify(node);
		}

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
				configuration.execute(node);
			}
		}
	}
}
