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
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.reflect.TypeOf;

import java.util.Optional;
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
		eventPublisher.subscribe(new DomainObjectEventSubscriber<DomainObjectCreated<T>>() {
			@Override
			@SuppressWarnings("unchecked")
			public void handle(DomainObjectCreated<T> event) {
				if (entityType.isInstance(event.getObject())) {
					assert knownObjects.isKnown((TypeAwareDomainObjectIdentifier<T>) event.getIdentifier()) : "Entity created without being discovered.";
				}
			}

			@Override
			public Class<? extends DomainObjectCreated<T>> subscribedToEventType() {
				return new TypeOf<DomainObjectCreated<T>>() {}.getConcreteClass();
			}
		});
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

	public boolean anyKnownIdentifier(Predicate<? super TypeAwareDomainObjectIdentifier<? extends T>> predicate) {
		return knownObjects.anyMatch(predicate);
	}

	@Override
	public Optional<TypeAwareDomainObjectIdentifier<? extends T>> findKnownIdentifier(Predicate<? super TypeAwareDomainObjectIdentifier<? extends T>> predicate) {
		return knownObjects.find(predicate);
	}
}
