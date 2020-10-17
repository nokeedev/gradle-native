package dev.nokee.model.internal;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import org.gradle.api.reflect.TypeOf;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public final class KnownDomainObjects<T> {
	private final Set<TypeAwareDomainObjectIdentifier<? extends T>> knownObjects = new LinkedHashSet<>();

	public KnownDomainObjects(Class<T> entityType, DomainObjectEventPublisher eventPublisher) {
		this(entityType, eventPublisher, identifier -> {});
	}

	public KnownDomainObjects(Class<T> entityType, DomainObjectEventPublisher eventPublisher, Consumer<? super TypeAwareDomainObjectIdentifier<? extends T>> whenDomainObjectDiscovered) {
		eventPublisher.subscribe(new DomainObjectEventSubscriber<DomainObjectDiscovered<T>>() {
			@Override
			public void handle(DomainObjectDiscovered<T> event) {
				if (entityType.isAssignableFrom(event.getType())) {
					assert !knownObjects.contains(event.getIdentifier()) : "Entity already known, duplicated event.";
					knownObjects.add(event.getIdentifier());
					whenDomainObjectDiscovered.accept(event.getIdentifier());
				}
			}

			@Override
			public Class<? extends DomainObjectDiscovered<T>> subscribedToEventType() {
				return new TypeOf<DomainObjectDiscovered<T>>() {}.getConcreteClass();
			}
		});
	}

	public void assertKnownObject(TypeAwareDomainObjectIdentifier<? extends T> identifier) {
		checkNotNull(identifier);
		checkArgument(knownObjects.contains(identifier), "Unknown entity identified as %s.", identifier);
	}

	public Set<TypeAwareDomainObjectIdentifier<? extends T>> filter(Predicate<? super TypeAwareDomainObjectIdentifier<? extends T>> predicate) {
		return knownObjects.stream().filter(predicate).collect(ImmutableSet.toImmutableSet());
	}

	public Optional<TypeAwareDomainObjectIdentifier<? extends T>> find(Predicate<? super TypeAwareDomainObjectIdentifier<? extends T>> predicate) {
		return knownObjects.stream().filter(predicate).collect(toAtMostOneElement());
	}

	public boolean anyMatch(Predicate<? super TypeAwareDomainObjectIdentifier<? extends T>> predicate) {
		return knownObjects.stream().anyMatch(predicate);
	}

	private static <T> Collector<T, ?, Optional<T>> toAtMostOneElement() {
		return Collectors.collectingAndThen(
			Collectors.toList(),
			list -> Optional.of(list).map(it -> Iterables.getOnlyElement(it, null))
		);
	}

	public boolean isKnown(TypeAwareDomainObjectIdentifier<T> identifier) {
		return knownObjects.contains(identifier);
	}

	public void forEach(Consumer<? super TypeAwareDomainObjectIdentifier<? extends T>> action) {
		knownObjects.forEach(action);
	}
}
