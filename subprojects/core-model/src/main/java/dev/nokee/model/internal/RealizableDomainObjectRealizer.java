package dev.nokee.model.internal;

import dev.nokee.model.DomainObjectIdentifier;

/**
 * Utility class to realize identifiable domain object which were discovered.
 */
public interface RealizableDomainObjectRealizer {
	/**
	 * Realize the specified identifier and parents.
	 * It's a sort-of old school software model-ish behavior.
	 *
	 * @param identifier the identifier to realize
	 * @param <T> the expected list type to return
	 * @return the specified identifier to realize.
	 */
	<T extends DomainObjectIdentifier> T ofElement(T identifier);
}
