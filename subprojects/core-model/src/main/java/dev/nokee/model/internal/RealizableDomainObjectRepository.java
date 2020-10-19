package dev.nokee.model.internal;

import org.gradle.api.provider.Provider;

import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

public interface RealizableDomainObjectRepository<T> {
	Set<T> filter(Predicate<? super TypeAwareDomainObjectIdentifier<? extends T>> predicate);
	Provider<Set<T>> filtered(Predicate<? super TypeAwareDomainObjectIdentifier<? extends T>> predicate);
	<S extends T> S get(TypeAwareDomainObjectIdentifier<S> identifier);
	<S extends T> Provider<S> identified(TypeAwareDomainObjectIdentifier<S> identifier);

	boolean anyKnownIdentifier(Predicate<? super TypeAwareDomainObjectIdentifier<? extends T>> predicate);
	Optional<TypeAwareDomainObjectIdentifier<? extends T>> findKnownIdentifier(Predicate<? super TypeAwareDomainObjectIdentifier<? extends T>> predicate);
}
