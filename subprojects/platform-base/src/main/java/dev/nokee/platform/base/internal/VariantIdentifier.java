package dev.nokee.platform.base.internal;

import dev.nokee.model.internal.DomainObjectIdentifierInternal;
import lombok.Value;

import java.util.Optional;

@Value
public class VariantIdentifier implements DomainObjectIdentifierInternal {
	String unambiguousName;
	ComponentIdentifier componentIdentifier;

	@Override
	public Optional<ComponentIdentifier> getParentIdentifier() {
		return Optional.of(componentIdentifier);
	}
}
