package dev.nokee.model.internal;

import dev.nokee.model.DomainObjectIdentifier;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.UnknownDomainObjectException;

import static dev.nokee.model.internal.DomainObjectIdentifierUtils.*;

public final class NamedDomainObjectConfigurer<T> {
	private final Class<T> entityType;
	private final KnownDomainObjects<T> knownEntities;
	private final DomainObjectConfigurer<T> configurer;

	public NamedDomainObjectConfigurer(Class<T> entityType, KnownDomainObjects<T> knownEntities, DomainObjectConfigurer<T> configurer) {
		this.entityType = entityType;
		this.knownEntities = knownEntities;
		this.configurer = configurer;
	}

	@SuppressWarnings("unchecked")
	private TypeAwareDomainObjectIdentifier<T> identifierByName(DomainObjectIdentifier owner, String name) {
		return knownEntities.find(directlyOwnedBy(owner).and(named(name)).and(withType(entityType))).map(TypeAwareDomainObjectIdentifier.class::cast).orElseThrow(() -> createNotFoundException(owner, name));
	}

	public <S extends T> void configure(DomainObjectIdentifier owner, String name, Class<S> type, Action<? super S> action) {
		val identifier = identifierByName(owner, name);
		try {
			configurer.configure(castIdentifier(type, identifier), action);
		} catch (RuntimeException ex) {
			throw createWrongTypeException(owner, name, type, identifier.getType(), ex);
		}
	}

	private UnknownDomainObjectException createNotFoundException(DomainObjectIdentifier owner, String name) {
		return new UnknownDomainObjectException(String.format("%s with name '%s' and directly owned by %s not found.", entityType.getSimpleName(), name, owner.toString()));
	}

	private InvalidUserDataException createWrongTypeException(DomainObjectIdentifier owner, String name, Class expected, Class actual, RuntimeException ex) {
		return new InvalidUserDataException(String.format("The domain object '%s' (%s) directly owned by %s is not a subclass of the given type (%s).", name, actual.getCanonicalName(), owner.toString(), expected.getCanonicalName()), ex);
	}
}
