package dev.nokee.model.internal.registry;

import com.google.common.collect.ImmutableList;
import dev.nokee.model.internal.core.*;
import lombok.val;
import org.gradle.api.model.ObjectFactory;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.google.common.base.Predicates.alwaysTrue;

public final class DefaultModelRegistry implements ModelRegistry, ModelConfigurer, ModelLookup {
	private final ObjectFactory objectFactory;
	private final Map<ModelPath, ModelNode> nodes = new LinkedHashMap<>();
	private final List<ModelConfiguration> configurations = new ArrayList<>();
	private final NodeStateListener nodeStateListener = new NodeStateListener();

	public DefaultModelRegistry(ObjectFactory objectFactory) {
		this.objectFactory = objectFactory;
		nodes.put(ModelPath.root(), createRootNode().register());
	}

	private static ModelNode createRootNode() {
		return new ModelNode(ModelPath.root(), Collections.emptyList());
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
		return new ModelNode(registration.getPath(), registration.getProjections(), this, nodeStateListener);
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

		private void notify(ModelNode node) {
			for (val configuration : configurations) {
				configuration.notifyFor(node);
			}
		}
	}

	private static final class ModelConfiguration {
		private final Predicate<ModelNode> predicate;
		private final ModelAction action;

		private ModelConfiguration(ModelSpec spec, ModelAction action) {
			Objects.requireNonNull(spec);
			this.predicate = toPathPredicate(spec).and(toParentPredicate(spec)).and(toAncestorPredicate(spec)).and(spec::isSatisfiedBy);
			this.action = Objects.requireNonNull(action);
		}

		private static Predicate<ModelNode> toPathPredicate(ModelSpec spec) {
			return spec.getPath().map(ModelConfiguration::asPathPredicate).orElse(alwaysTrue());
		}

		private static Predicate<ModelNode> asPathPredicate(ModelPath path) {
			return node -> node.getPath().equals(path);
		}

		private static Predicate<ModelNode> toParentPredicate(ModelSpec spec) {
			return spec.getParent().map(ModelConfiguration::asParentPredicate).orElse(alwaysTrue());
		}

		private static Predicate<ModelNode> asParentPredicate(ModelPath parent) {
			return node -> {
				val parentPath = node.getPath().getParent();
				return parentPath.isPresent() && parentPath.get().equals(parent);
			};
		}

		private static Predicate<ModelNode> toAncestorPredicate(ModelSpec spec) {
			return spec.getAncestor().map(ModelConfiguration::asAncestorPredicate).orElse(alwaysTrue());
		}

		private static Predicate<ModelNode> asAncestorPredicate(ModelPath ancestor) {
//			return node -> node.getPath().equals(path);
			return alwaysTrue();
		}

		public void notifyFor(ModelNode node) {
			if (predicate.test(node)) {
				action.execute(node);
			}
		}
	}
}
