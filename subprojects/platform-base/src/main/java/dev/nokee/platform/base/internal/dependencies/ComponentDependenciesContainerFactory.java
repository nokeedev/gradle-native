package dev.nokee.platform.base.internal.dependencies;

import dev.nokee.model.DomainObjectIdentifier;

public interface ComponentDependenciesContainerFactory {
	ComponentDependenciesContainer create(DomainObjectIdentifier identifier);
}
