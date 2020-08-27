package dev.nokee.platform.base.internal.tasks;

import dev.nokee.model.DomainObjectIdentifier;
import lombok.val;
import lombok.var;
import org.gradle.api.Task;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

final class KnownTaskIdentifierRegistryImpl implements KnownTaskIdentifierRegistry {
	private final Set<TaskIdentifier<? extends Task>> knownIdentifiers = new HashSet<>();

	public void add(TaskIdentifier<? extends Task> identifier) {
		assert identifier != null;
		val identifierAdded = knownIdentifiers.add(identifier);
		assert identifierAdded : "Task identifier collision detected.";
	}

	public Set<String> getTaskNamesFor(DomainObjectIdentifier identifier) {
		Predicate<TaskIdentifier<?>> predicate = (TaskIdentifier<?> id) -> {
			var parentIdentifier = id.getOwnerIdentifier();
			while (!identifier.getClass().isInstance(parentIdentifier)) {
				if (parentIdentifier.getParentIdentifier().isPresent()) {
					parentIdentifier = parentIdentifier.getParentIdentifier().get();
				} else {
					return false;
				}
			}
			return identifier.equals(parentIdentifier);
		};
		return knownIdentifiers.stream().filter(predicate).map(TaskIdentifier::getTaskName).collect(Collectors.toSet());
	}
}
