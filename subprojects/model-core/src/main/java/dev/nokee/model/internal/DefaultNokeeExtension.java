package dev.nokee.model.internal;

import dev.nokee.model.NokeeExtension;
import dev.nokee.model.registry.ModelRegistry;
import groovy.lang.Closure;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.PolymorphicDomainObjectContainer;
import org.gradle.api.model.ObjectFactory;
import org.gradle.util.ConfigureUtil;

import javax.inject.Inject;

import static dev.nokee.utils.NamedDomainObjectCollectionUtils.whenElementKnown;

abstract /*final*/ class DefaultNokeeExtension implements NokeeExtension {
	private final ModelRegistry modelRegistry;
	private final ObjectFactory objectFactory;
	private final DefaultNamedDomainObjectRegistry registry;

	@Inject
	public DefaultNokeeExtension(DefaultNamedDomainObjectRegistry registry, ObjectFactory objectFactory) {
		this.modelRegistry = new DefaultModelRegistry(objectFactory, registry);
		this.objectFactory = objectFactory;
		this.registry = registry;
	}

	@Override
	public ModelRegistry getModelRegistry() {
		return modelRegistry;
	}

	@Override
	public <T> DefaultNokeeExtension bridgeContainer(NamedDomainObjectContainer<T> container) {
		val bridgedContainer = new BridgedDomainObjectContainerRegistry<>(new NamedDomainObjectContainerRegistry.NamedContainerRegistry<>(container), modelRegistry);
		registry.registerContainer(bridgedContainer);
		whenElementKnown(container, bridgedContainer);
		return this;
	}

	@Override
	public <T> DefaultNokeeExtension bridgeContainer(PolymorphicDomainObjectContainer<T> container) {
		val bridgedContainer = new BridgedDomainObjectContainerRegistry<>(new NamedDomainObjectContainerRegistry.PolymorphicContainerRegistry<>(container), modelRegistry);
		registry.registerContainer(bridgedContainer);
		whenElementKnown(container, bridgedContainer);
		return this;
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
