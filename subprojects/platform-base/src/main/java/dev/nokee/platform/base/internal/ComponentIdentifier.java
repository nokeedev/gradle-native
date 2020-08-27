package dev.nokee.platform.base.internal;

import dev.nokee.model.internal.DomainObjectIdentifierInternal;
import lombok.Value;

import java.util.Optional;

@Value
public class ComponentIdentifier implements DomainObjectIdentifierInternal {
	String name;
	ProjectIdentifier projectIdentifier;

	@Override
	public Optional<ProjectIdentifier> getParentIdentifier() {
		return Optional.of(projectIdentifier);
	}
}
