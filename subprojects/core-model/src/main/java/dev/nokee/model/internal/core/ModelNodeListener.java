package dev.nokee.model.internal.core;

/**
 * A model node state listener.
 */
public interface ModelNodeListener {
	/**
	 * A listener that does nothing on all callback.
	 *
	 * @return a model node listener that perform no operation on callbacks, never null
	 */
	static ModelNodeListener noOpListener() {
		return new ModelNodeListener() {
			@Override
			public void created(ModelNode node) {
				// do nothing
			}

			@Override
			public void initialized(ModelNode modelNode) {
				// do nothing
			}

			@Override
			public void registered(ModelNode modelNode) {
				// do nothing
			}

			@Override
			public void realized(ModelNode node) {
				// do nothing
			}
		};
	}

	/**
	 * When the model node transition to {@link ModelNode.State#Created}.
	 *
	 * @param node  the model node that transitioned to the created state
	 */
	void created(ModelNode node);

	/**
	 * When the model node transition to {@link ModelNode.State#Initialized}.
	 *
	 * @param node  the model node that transitioned to the initialized state
	 */
	void initialized(ModelNode node);

	/**
	 * When the model node transition to {@link ModelNode.State#Registered}.
	 *
	 * @param node  the model node that transitioned to the registered state
	 */
	void registered(ModelNode node);

	/**
	 * When the model node transition to {@link ModelNode.State#Realized}.
	 *
	 * @param node  the model node that transitioned to the realized state
	 */
	void realized(ModelNode node);
}
