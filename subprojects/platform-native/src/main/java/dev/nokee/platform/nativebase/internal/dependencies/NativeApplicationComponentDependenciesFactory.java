package dev.nokee.platform.nativebase.internal.dependencies;

import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.platform.base.internal.dependencies.ComponentDependenciesContainerFactory;

import javax.inject.Inject;

public final class NativeApplicationComponentDependenciesFactory {
	private final ComponentDependenciesContainerFactory factory;

	@Inject
	public NativeApplicationComponentDependenciesFactory(ComponentDependenciesContainerFactory factory) {
		this.factory = factory;
	}

	public NativeApplicationComponentDependenciesInternal create(DomainObjectIdentifier identifier) {
		return new NativeApplicationComponentDependenciesImpl(factory.create(identifier));
	}
}

