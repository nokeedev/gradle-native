package dev.nokee.model.internal;

import dev.nokee.model.NokeeExtension;
import dev.nokee.model.registry.ModelRegistry;

import javax.inject.Inject;

/*final*/ abstract class DefaultNokeeExtension implements NokeeExtension {
	private final ModelRegistry modelRegistry = new DefaultModelRegistry();

	@Inject
	public DefaultNokeeExtension() {

	}

	@Override
	public ModelRegistry getModelRegistry() {
		return modelRegistry;
	}
}
