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

import lombok.val;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;

public final class DomainObjectEventPublisherImpl implements DomainObjectEventPublisher {
	private final List<DomainObjectEventSubscriber<?>> subscribers = new ArrayList<>();
	private final List<DomainObjectEvent> events = new ArrayList<>();

	public DomainObjectEventPublisherImpl() {}

	@Override
	public <T extends DomainObjectEvent> void publish(T event) {
		requireNonNull(event);
		subscribers.stream().filter(subscribedToEventType(event.getClass())).forEach(subscriber -> {
			@SuppressWarnings("unchecked")
			val castedSubscriber = (DomainObjectEventSubscriber<T>) subscriber;
			castedSubscriber.handle(event);
		});
		events.add(event);
	}

	private Predicate<DomainObjectEventSubscriber<?>> subscribedToEventType(Class<?> eventType) {
		return subscriber -> subscriber.subscribedToEventType().isAssignableFrom(eventType);
	}

	@Override
	public <T extends DomainObjectEvent> void subscribe(DomainObjectEventSubscriber<T> subscriber) {
		val subscribedEventType = subscriber.subscribedToEventType();
		for (int i = 0 ; i < events.size(); ++i) {
			val event = events.get(i);
			if (subscribedEventType.isAssignableFrom(event.getClass())) {
				subscriber.handle((T) event);
			}
		}
		subscribers.add(requireNonNull(subscriber));
	}
}
