/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.nokee.model.internal;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import org.gradle.api.reflect.TypeOf;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public final class KnownDomainObjects<T> {
	private final List<TypeAwareDomainObjectIdentifier<? extends T>> knownObjects = new ArrayList<>();

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
		for (int i = 0; i < knownObjects.size(); i++) {
			action.accept(knownObjects.get(i));
		}
	}
}
