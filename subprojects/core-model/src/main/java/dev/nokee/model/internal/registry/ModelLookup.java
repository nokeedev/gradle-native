package dev.nokee.model.internal.registry;

import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelPath;

/**
 * A read-only supplier of {@link ModelNode}.
 */
public interface ModelLookup {
	/**
	 * Returns the model node for the specified path.
	 *
	 * @param path  a model node path to retrieve
	 * @return the {@link ModelNode} for the specified path, never null.
	 */
	ModelNode get(ModelPath path);

	/**
	 * Returns an always failing model lookup.
	 *
	 * @return a model lookup that always fails.
	 */
	static ModelLookup failingLookup() {
		return new ModelLookup() {
			@Override
			public ModelNode get(ModelPath path) {
				throw new UnsupportedOperationException();
			}
		};
	}
}
