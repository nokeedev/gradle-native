package dev.nokee.model.internal;

import com.google.common.collect.ImmutableSet;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;

import java.util.Set;
import java.util.function.Predicate;

public abstract class AbstractRealizableDomainObjectRepository<T> implements RealizableDomainObjectRepository<T> {
	private final KnownDomainObjects<T> knownObjects;
	private final DomainObjects<T> objects;
	private final RealizableDomainObjectRealizer realizer;
	private final ProviderFactory providerFactory;

	public AbstractRealizableDomainObjectRepository(Class<T> entityType, DomainObjectEventPublisher eventPublisher, RealizableDomainObjectRealizer realizer, ProviderFactory providerFactory) {
		this.realizer = realizer;
		this.providerFactory = providerFactory;
		this.knownObjects = new KnownDomainObjects<>(entityType, eventPublisher);
		this.objects = new DomainObjects<>(entityType, eventPublisher);
	}

	public Set<T> filter(Predicate<? super TypeAwareDomainObjectIdentifier<? extends T>> predicate) {
		return knownObjects.filter(predicate).stream()
			.map(realizer::ofElement)
			.map(objects::getByIdentifier)
			.collect(ImmutableSet.toImmutableSet());
	}

	public Provider<Set<T>> filtered(Predicate<? super TypeAwareDomainObjectIdentifier<? extends T>> predicate) {
		return providerFactory.provider(() -> filter(predicate));
	}

	public <S extends T> S get(TypeAwareDomainObjectIdentifier<S> identifier) {
		knownObjects.assertKnownObject(identifier);
		realizer.ofElement(identifier);
		return identifier.getType().cast(objects.getByIdentifier(identifier));
	}

	public <S extends T> Provider<S> identified(TypeAwareDomainObjectIdentifier<S> identifier) {
		knownObjects.assertKnownObject(identifier);
		return providerFactory.provider(() -> get(identifier));
	}
}
