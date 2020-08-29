package dev.nokee.platform.base.internal.tasks;

import dev.nokee.model.internal.DomainObjectIdentifierInternal;
import lombok.Value;
import org.gradle.api.Task;

import java.util.Optional;

@Value
class TaskIdentifier<T extends Task> implements DomainObjectIdentifierInternal {
	String taskName;
	Class<T> type;
	DomainObjectIdentifierInternal ownerIdentifier;

	@Override
	public Optional<? extends DomainObjectIdentifierInternal> getParentIdentifier() {
		return Optional.of(ownerIdentifier);
	}

	@Override
	public String getDisplayName() {
		throw new UnsupportedOperationException(); // for now...
	}
}
