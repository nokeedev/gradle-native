package dev.nokee.model.internal.core;

import dev.nokee.internal.Factory;
import dev.nokee.model.internal.type.ModelType;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.val;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static dev.nokee.model.internal.core.ModelRegistration.builder;
import static dev.nokee.model.internal.core.NodePredicate.self;

// The major difference between {@link ModelRegistration} and {@link NodeRegistration} is the fact that ModelRegistration is absolute, e.g. starts from root node where NodeRegistration is relative, e.g. relative to a model node.
@ToString
@EqualsAndHashCode
public final class NodeRegistration<T> {
	private final String name;
	private final ModelType<T> type;
	private final List<ModelProjection> projections = new ArrayList<>();
	private final List<NodeActionRegistration> actionRegistrations = new ArrayList<>();

	private NodeRegistration(String name, ModelType<T> type, ModelProjection defaultProjection) {
		this.name = name;
		this.type = type;
		projections.add(defaultProjection);
	}

	ModelRegistration<T> scope(ModelPath path) {
		val builder = builder()
			.withPath(path.child(name))
			.withDefaultProjectionType(type);
		projections.forEach(builder::withProjection);
		actionRegistrations.stream().map(it -> it.scope(path.child(name))).forEach(builder::action);
		return builder.build();
	}

	public static <T> NodeRegistration<T> of(String name, ModelType<T> type, Object... parameters) {
		return new NodeRegistration<>(name, type, ModelProjections.managed(type, parameters));
	}

	public static <T> NodeRegistration<T> unmanaged(String name, ModelType<T> type, Factory<T> factory) {
		return new NodeRegistration<>(name, type, ModelProjections.createdUsing(type, factory));
	}

	public NodeRegistration<T> withProjection(ModelProjection projection) {
		projections.add(projection);
		return this;
	}

	// TODO: Consider for removal
	public NodeRegistration<T> action(Predicate<? super ModelNode> predicate, ModelAction action) {
		action(self(predicate), action);
		return this;
	}

	public NodeRegistration<T> action(NodePredicate predicate, ModelAction action) {
		actionRegistrations.add(new NodeActionRegistration(predicate, action));
		return this;
	}

	private static final class NodeActionRegistration {
		private final NodePredicate predicate;
		private final ModelAction action;

		public NodeActionRegistration(NodePredicate predicate, ModelAction action) {
			this.predicate = predicate;
			this.action = action;
		}

		public ModelAction scope(ModelPath path) {
			return ModelActions.onlyIf(predicate.scope(path), action);
		}
	}
}
