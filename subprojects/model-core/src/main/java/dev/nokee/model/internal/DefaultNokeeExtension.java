package dev.nokee.model.internal;

import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.NokeeExtension;
import dev.nokee.model.dsl.ModelNode;
import dev.nokee.model.registry.DomainObjectRegistry;
import dev.nokee.model.registry.ModelRegistry;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectProvider;

import javax.inject.Inject;

/*final*/ abstract class DefaultNokeeExtension implements NokeeExtension {
	private final ModelRegistry modelRegistry = new DefaultModelRegistry();
	private final ModelNode model = new DefaultModelNodeDsl(new DomainObjectRegistry() {
		@Override
		public <T> NamedDomainObjectProvider<T> register(DomainObjectIdentifier identifier, Class<T> type) {
			throw new UnsupportedOperationException();
		}
	}, modelRegistry.getRoot());

	@Inject
	public DefaultNokeeExtension() {}

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
