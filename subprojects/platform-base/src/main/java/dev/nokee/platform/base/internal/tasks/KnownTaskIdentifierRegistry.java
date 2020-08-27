package dev.nokee.platform.base.internal.tasks;

import dev.nokee.model.DomainObjectIdentifier;
import org.gradle.api.Task;

import java.util.Set;

interface KnownTaskIdentifierRegistry {
	void add(TaskIdentifier<? extends Task> identifier);

	Set<String> getTaskNamesFor(DomainObjectIdentifier identifier);
}
