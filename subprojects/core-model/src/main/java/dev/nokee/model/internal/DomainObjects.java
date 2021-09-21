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

import dev.nokee.model.DomainObjectIdentifier;
import org.gradle.api.reflect.TypeOf;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkNotNull;

public final class DomainObjects<T> {
	private final Map<DomainObjectIdentifier, T> objects = new LinkedHashMap<>();

	public DomainObjects(Class<T> entityType, DomainObjectEventPublisher eventPublisher) {
		eventPublisher.subscribe(new DomainObjectEventSubscriber<DomainObjectRealized<T>>() {
			@Override
			public void handle(DomainObjectRealized<T> event) {
				if (entityType.isInstance(event.getObject())) {
					assert !objects.containsKey(event.getIdentifier()) : "Entity already realized, duplicated event.";
					objects.put(event.getIdentifier(), event.getObject());
				}
			}

			@Override
			public Class<? extends DomainObjectRealized<T>> subscribedToEventType() {
				return new TypeOf<DomainObjectRealized<T>>() {}.getConcreteClass();
			}
		});
	}

	public DomainObjects(Class<T> entityType, DomainObjectEventPublisher eventPublisher, Consumer<? super T> whenObjectCreated) {
		eventPublisher.subscribe(new DomainObjectEventSubscriber<DomainObjectCreated<T>>() {
			@Override
			public void handle(DomainObjectCreated<T> event) {
				if (entityType.isInstance(event.getObject())) {
					assert !objects.containsKey(event.getIdentifier()) : "Entity already realized, duplicated event.";
					objects.put(event.getIdentifier(), event.getObject());
					whenObjectCreated.accept(event.getObject());
				}
			}

			@Override
			public Class<? extends DomainObjectCreated<T>> subscribedToEventType() {
				return new TypeOf<DomainObjectCreated<T>>() {}.getConcreteClass();
			}
		});
	}

	public void forEach(Consumer<? super T> action) {
		objects.values().forEach(action);
	}

	public T getByIdentifier(DomainObjectIdentifier identifier) {
		return checkNotNull(objects.get(identifier), "Entity for %s was not created.", identifier.toString());
	}

	public Optional<T> findByIdentifier(DomainObjectIdentifier identifier) {
		return Optional.ofNullable(objects.get(identifier));
	}

	public DomainObjectIdentifier lookupIdentifier(T object) {
		return objects.entrySet().stream()
			.filter(it -> it.getValue().equals(object))
			.findFirst()
			.map(Map.Entry::getKey)
			.orElseThrow(() -> new RuntimeException("Unknown object"));
	}
}
