package dev.nokee.model.internal.core;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public abstract class NodeAction {
	abstract ModelAction scope(ModelPath path);

	/**
	 * Returns node action that will scope to {@link ModelActions#doNothing()}.
	 *
	 * @return a node action that will always scope to {@link ModelActions#doNothing()}, never null.
	 */
	public static NodeAction doNothing() {
		return new NodeAction() {
			@Override
			ModelAction scope(ModelPath path) {
				return ModelActions.doNothing();
			}
		};
	}
}
