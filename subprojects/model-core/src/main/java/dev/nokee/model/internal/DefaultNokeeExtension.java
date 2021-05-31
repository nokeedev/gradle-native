package dev.nokee.model.internal;

import dev.nokee.model.NokeeExtension;
import dev.nokee.model.dsl.ModelNode;
import dev.nokee.model.registry.ModelRegistry;
import org.gradle.api.Action;

import javax.inject.Inject;

/*final*/ abstract class DefaultNokeeExtension implements NokeeExtension {
	private final ModelRegistry modelRegistry = new DefaultModelRegistry();
	private final ModelNode model;

	@Inject
	public DefaultNokeeExtension(NamedDomainObjectRegistry registry) {
		this.model = new DefaultModelNodeDsl(registry, modelRegistry.getRoot());
	}

	@Override
	public ModelRegistry getModelRegistry() {
		return modelRegistry;
	}

	@Override
	public ModelNode getModel() {
		return model;
	}

	@Override
	public void model(Action<? super ModelNode> action) {
		action.execute(model);
	}
}
