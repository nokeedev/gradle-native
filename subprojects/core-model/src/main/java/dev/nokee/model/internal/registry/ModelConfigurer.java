package dev.nokee.model.internal.registry;

import dev.nokee.model.internal.core.ModelAction;
import dev.nokee.model.internal.core.ModelSpec;

// TODO: It should probably be more an observer of the model where the ModelAction would configure.
//   As we don't have much intention to tightly control the lifecycle of the model like the software model there isn't a need for specifying the role of the action.
//   Instead, the action should specify what state it expect a node to be in before executing.
//   For example, nodes starts in Registered which listener can trigger on to add sub-links.
//   When the node transition to Discovered, listener can use that as "element known" configuration.
//   A user configuring a listener for Discovered but the node is created should still be called as the node *is discovered* but is also created.
public interface ModelConfigurer {
	// TODO: We should probably merge spec and action together and enhance ModelAction with ModelPredicate to enable fast filtering and indexing
	void configureMatching(ModelSpec spec, ModelAction action);

	static ModelConfigurer failingConfigurer() {
		return new ModelConfigurer() {
			@Override
			public void configureMatching(ModelSpec spec, ModelAction action) {
				throw new UnsupportedOperationException("This instance always fails.");
			}
		};
	}
}
