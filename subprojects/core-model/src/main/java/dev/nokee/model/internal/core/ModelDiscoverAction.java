package dev.nokee.model.internal.core;

public abstract class ModelDiscoverAction implements ModelAction {
	@Override
	public final void execute(ModelNode node) {
		// TODO: Should be discovered
		if (node.getState().equals(ModelNode.State.Registered)) {
			execute(new Context(node));
		}
	}

	protected abstract void execute(Context context);

	protected static class Context {
		private final ModelNode node;

		public Context(ModelNode node) {
			this.node = node;
		}

		public Context applyTo(NodeAction action) {
			node.applyTo(action);
			return this;
		}

		public Context register(NodeRegistration<?> registration) {
			node.register(registration);
			return this;
		}

		public ModelPath getPath() {
			return node.getPath();
		}
	}
}
