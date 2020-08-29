package dev.nokee.platform.jni.internal;

import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.platform.base.internal.dependencies.ComponentDependenciesContainerFactory;

import javax.inject.Inject;

public final class DefaultJavaNativeInterfaceNativeComponentDependenciesFactory {
	private final ComponentDependenciesContainerFactory factory;

	@Inject
	public DefaultJavaNativeInterfaceNativeComponentDependenciesFactory(ComponentDependenciesContainerFactory factory) {
		this.factory = factory;
	}

	public DefaultJavaNativeInterfaceNativeComponentDependencies create(DomainObjectIdentifier identifier) {
		return new DefaultJavaNativeInterfaceNativeComponentDependencies(factory.create(identifier));
	}
}
