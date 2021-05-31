package dev.nokee.model.internal;

import dev.nokee.model.NokeeExtension;
import dev.nokee.model.dsl.ModelNode;
import dev.nokee.model.registry.ModelRegistry;
import dev.nokee.utils.NamedDomainObjectCollectionUtils;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.PolymorphicDomainObjectContainer;

import javax.inject.Inject;

import static dev.nokee.utils.NamedDomainObjectCollectionUtils.whenElementKnown;

/*final*/ abstract class DefaultNokeeExtension implements NokeeExtension {
	private final ModelRegistry modelRegistry = new DefaultModelRegistry();
	private final DefaultNamedDomainObjectRegistry registry;
	private final ModelNode model;

	@Inject
	public DefaultNokeeExtension(DefaultNamedDomainObjectRegistry registry) {
		this.model = new DefaultModelNodeDsl(registry, modelRegistry.getRoot());
		this.registry = registry;
	}

	@Override
	public ModelRegistry getModelRegistry() {
		return modelRegistry;
	}

	@Override
	public <T> void bridgeContainer(NamedDomainObjectContainer<T> container) {
		registry.registerContainer(new NamedDomainObjectContainerRegistry.NamedContainerRegistry<>(container));
		whenElementKnown(container, new RegisterModelProjection<>(modelRegistry));
	}

	@Override
	public <T> void bridgeContainer(PolymorphicDomainObjectContainer<T> container) {
		registry.registerContainer(new NamedDomainObjectContainerRegistry.PolymorphicContainerRegistry<>(container));
		whenElementKnown(container, new RegisterModelProjection<>(modelRegistry));
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
