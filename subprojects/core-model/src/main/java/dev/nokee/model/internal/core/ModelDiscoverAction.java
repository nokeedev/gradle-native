package dev.nokee.model.internal.core;

import dev.nokee.model.KnownDomainObject;
import dev.nokee.model.internal.registry.ModelNodeBackedKnownDomainObject;
import dev.nokee.model.internal.type.ModelType;

import static com.google.common.base.Preconditions.checkArgument;

public abstract class ModelDiscoverAction implements ModelAction {
	@Override
	public final void execute(ModelNode node) {
		// TODO: Should be discovered
		if (node.getState().equals(ModelNode.State.Registered)) {
			// NOTE: The contextual node should not be accessed from the action, it's simply for contextualizing the action execution.
			ModelNodeContext.of(node).execute(() -> execute(new Context(node)));
		}
	}

	protected abstract void execute(Context context);

	public static class Context {
		private final ModelNode node;

		public Context(ModelNode node) {
			this.node = node;
		}

		public ModelPath getPath() {
			return node.getPath();
		}

		public Context applyTo(NodeAction action) {
			node.applyTo(action);
			return this;
		}

		public <T> KnownDomainObject<T> register(NodeRegistration<T> registration) {
			return new ModelNodeBackedKnownDomainObject<>(registration.getDefaultProjectionType(), ModelNodes.of(node.register(registration)));
		}

		public <T> KnownDomainObject<T> projectionOf(ModelType<T> type) {
			checkArgument(node.canBeViewedAs(type));
			return new ModelNodeBackedKnownDomainObject<>(type, node);
		}
	}
}
