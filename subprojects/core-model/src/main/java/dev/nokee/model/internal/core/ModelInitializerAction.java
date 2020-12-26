package dev.nokee.model.internal.core;

public abstract class ModelInitializerAction implements ModelAction {
	@Override
	public final void execute(ModelNode node) {
		if (node.getState().equals(ModelNode.State.Created)) {
			execute(new Context(node));
		}
	}

	public abstract void execute(Context context);

	public static class Context {
		private final ModelNode node;

		public Context(ModelNode node) {
			this.node = node;
		}

		public Context applyTo(NodeAction action) {
			node.applyTo(action);
			return this;
		}

		public Context addProjection(ModelProjection projection) {
			node.addProjection(projection);
			return this;
		}
	}
}
