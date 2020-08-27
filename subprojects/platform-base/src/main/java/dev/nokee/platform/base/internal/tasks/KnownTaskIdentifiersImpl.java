package dev.nokee.platform.base.internal.tasks;

import dev.nokee.model.DomainObjectIdentifier;

class KnownTaskIdentifiersImpl implements KnownTaskIdentifiers {
	private final DomainObjectIdentifier identifier;
	private final KnownTaskIdentifierRegistry knownIdentifierRegistry;

	public KnownTaskIdentifiersImpl(DomainObjectIdentifier identifier, KnownTaskIdentifierRegistry knownIdentifierRegistry) {
		this.identifier = identifier;
		this.knownIdentifierRegistry = knownIdentifierRegistry;
	}

	public boolean contains(String taskName) {
		assert taskName != null;
		return knownIdentifierRegistry.getTaskNamesFor(identifier).contains(taskName);
	}
}
