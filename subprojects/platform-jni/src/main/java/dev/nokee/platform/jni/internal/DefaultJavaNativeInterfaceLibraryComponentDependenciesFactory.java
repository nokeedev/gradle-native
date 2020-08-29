package dev.nokee.platform.jni.internal;

import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.platform.base.internal.dependencies.ComponentDependenciesContainerFactory;

import javax.inject.Inject;

public final class DefaultJavaNativeInterfaceLibraryComponentDependenciesFactory {
	private final ComponentDependenciesContainerFactory factory;

	@Inject
	public DefaultJavaNativeInterfaceLibraryComponentDependenciesFactory(ComponentDependenciesContainerFactory factory) {
		this.factory = factory;
	}

	public DefaultJavaNativeInterfaceLibraryComponentDependencies create(DomainObjectIdentifier identifier) {
		return new DefaultJavaNativeInterfaceLibraryComponentDependencies(factory.create(identifier));
	}
}
