package dev.nokee.model.internal.core;

import dev.nokee.model.internal.type.ModelType;

import static com.google.common.base.Preconditions.checkArgument;

public abstract class ModelMutateAction implements ModelAction {
	@Override
	public void execute(ModelNode node) {
		if (node.getState().equals(ModelNode.State.Realized)) {
			ModelNodeContext.of(node).execute(() -> execute(new Context(node)));
		}
	}

	public abstract void execute(Context context);

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

		public <T> T projectionOf(ModelType<T> type) {
			checkArgument(node.canBeViewedAs(type));
			return node.get(type);
		}
	}
}
