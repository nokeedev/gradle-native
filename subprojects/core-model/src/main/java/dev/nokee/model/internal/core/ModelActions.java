package dev.nokee.model.internal.core;

public final class ModelActions {
	private ModelActions() {}

	public static ModelAction doNothing() {
		return ModelNodeAction.DO_NOTHING;
	}

	private enum ModelNodeAction implements ModelAction {
		DO_NOTHING {
			@Override
			public void execute(ModelNode node) {
				// do nothing.
			}

			@Override
			public String toString() {
				return "ModelActions.doNothing()";
			}
		}
	}
}
