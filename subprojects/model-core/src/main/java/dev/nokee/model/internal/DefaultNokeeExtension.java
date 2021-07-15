package dev.nokee.model.internal;

import dev.nokee.model.NokeeExtension;
import dev.nokee.model.dsl.ModelNode;
import dev.nokee.model.registry.ModelRegistry;
import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.PolymorphicDomainObjectContainer;
import org.gradle.api.model.ObjectFactory;
import org.gradle.util.ConfigureUtil;

import javax.inject.Inject;

import static dev.nokee.utils.NamedDomainObjectCollectionUtils.whenElementKnown;

/*final*/ abstract class DefaultNokeeExtension implements NokeeExtension {
	private final ModelRegistry modelRegistry;
	private final ObjectFactory objectFactory;
	private final DefaultNamedDomainObjectRegistry registry;
	private final ModelNode model;

	@Inject
	public DefaultNokeeExtension(DefaultNamedDomainObjectRegistry registry, ObjectFactory objectFactory) {
		this.modelRegistry = new DefaultModelRegistry(objectFactory);
		this.objectFactory = objectFactory;
		this.model = new DefaultModelNodeDslFactory(registry, modelRegistry.allProjections(), objectFactory).create(modelRegistry.getRoot());
		this.registry = registry;
	}

	@Override
	public ModelRegistry getModelRegistry() {
		return modelRegistry;
	}

	@Override
	public <T> DefaultNokeeExtension bridgeContainer(NamedDomainObjectContainer<T> container) {
		registry.registerContainer(new NamedDomainObjectContainerRegistry.NamedContainerRegistry<>(container));
		whenElementKnown(container, new RegisterModelProjection<>(modelRegistry));
		return this;
	}

	@Override
	public <T> DefaultNokeeExtension bridgeContainer(PolymorphicDomainObjectContainer<T> container) {
		registry.registerContainer(new NamedDomainObjectContainerRegistry.PolymorphicContainerRegistry<>(container));
		whenElementKnown(container, new RegisterModelProjection<>(modelRegistry));
		return this;
	}

	@Override
	public ModelNode getModel() {
		return model;
	}

	@Override
	public void model(Action<? super ModelNode> action) {
		action.execute(model);
	}

	@Override
	public NokeeExtension configure(Action<? super NokeeExtension> action) {
		action.execute(this);
		return this;
	}

	@Override
	public NokeeExtension configure(Class<? extends Action<? super NokeeExtension>> actionClass) {
		objectFactory.newInstance(actionClass).execute(this);
		return this;
	}

	@Override
	public NokeeExtension configure(Closure closure) {
		ConfigureUtil.configureSelf(closure, this);
		return this;
	}
}
