package dev.nokee.platform.nativebase.internal.dependencies;

import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.platform.base.internal.dependencies.ComponentDependenciesContainerFactory;

import javax.inject.Inject;

public final class NativeLibraryComponentDependenciesFactory {
	private final ComponentDependenciesContainerFactory factory;

	@Inject
	public NativeLibraryComponentDependenciesFactory(ComponentDependenciesContainerFactory factory) {
		this.factory = factory;
	}

	public NativeLibraryComponentDependenciesInternal create(DomainObjectIdentifier identifier) {
		return new NativeLibraryComponentDependenciesImpl(factory.create(identifier));
	}
}
