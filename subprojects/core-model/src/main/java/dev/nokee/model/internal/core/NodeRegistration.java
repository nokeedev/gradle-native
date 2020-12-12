package dev.nokee.model.internal.core;

import dev.nokee.model.internal.registry.ManagedModelProjection;
import dev.nokee.model.internal.type.ModelType;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.val;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static dev.nokee.model.internal.core.ModelActions.onlyIf;
import static dev.nokee.model.internal.core.ModelRegistration.builder;

// The major difference between {@link ModelRegistration} and {@link NodeRegistration} is the fact that ModelRegistration is absolute, e.g. starts from root node where NodeRegistration is relative, e.g. relative to a model node.
@ToString
@EqualsAndHashCode
public final class NodeRegistration<T> {
	private final String name;
	private final ModelType<T> type;
	private final List<ModelProjection> projections = new ArrayList<>();
	private final List<ModelAction> actions = new ArrayList<>();

	private NodeRegistration(String name, ModelType<T> type) {
		this.name = name;
		this.type = type;
	}

	ModelRegistration<T> scope(ModelPath path) {
		val builder = builder()
			.withPath(path.child(name))
			.withDefaultProjectionType(type)
			.withProjection(ManagedModelProjection.of(type));
		projections.forEach(builder::withProjection);
		actions.forEach(builder::action);
		return builder.build();
	}

	public static <T> NodeRegistration<T> of(String name, ModelType<T> type) {
		return new NodeRegistration<>(name, type);
	}

	public NodeRegistration<T> withProjection(ModelProjection projection) {
		projections.add(projection);
		return this;
	}

	public NodeRegistration<T> action(Predicate<? super ModelNode> predicate, ModelAction action) {
		actions.add(onlyIf(predicate, action));
		return this;
	}
}
