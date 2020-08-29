package dev.nokee.platform.nativebase.internal.dependencies;

import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.platform.base.internal.dependencies.ComponentDependenciesContainerFactory;

import javax.inject.Inject;

public final class NativeComponentDependenciesFactory {
	private final ComponentDependenciesContainerFactory factory;

	@Inject
	public NativeComponentDependenciesFactory(ComponentDependenciesContainerFactory factory) {
		this.factory = factory;
	}

	public NativeComponentDependenciesInternal create(DomainObjectIdentifier identifier) {
		return new NativeComponentDependenciesImpl(factory.create(identifier));
	}
}
