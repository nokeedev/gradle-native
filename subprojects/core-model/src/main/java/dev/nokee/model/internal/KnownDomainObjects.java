package dev.nokee.model.internal;

import dev.nokee.model.DomainObjectFactory;
import dev.nokee.model.DomainObjectIdentifier;
import lombok.val;
import org.gradle.api.specs.Spec;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class KnownDomainObjects<I extends DomainObjectIdentifier, T> {
	private final Map<I, T> identifierToObjects = new HashMap<>();

	public <S extends T> S withKnownIdentifier(I identifier, DomainObjectFactory<S> factory) {
		val obj = factory.create(identifier);
		identifierToObjects.put(identifier, obj);
		return obj;
	}

	public Optional<T> findByIdentifier(Spec<? super I> spec) {
		return identifierToObjects.entrySet().stream().filter(it -> spec.isSatisfiedBy(it.getKey())).findFirst().map(Map.Entry::getValue);
	}

	public Optional<T> findByIdentifier(I identifier) {
		return Optional.ofNullable(identifierToObjects.get(identifier));
	}
}
