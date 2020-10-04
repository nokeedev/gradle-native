package dev.nokee.model.internal;

import lombok.val;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;

public final class DomainObjectEventPublisherImpl implements DomainObjectEventPublisher {
	private final List<DomainObjectEventSubscriber<?>> subscribers = new ArrayList<>();

	public DomainObjectEventPublisherImpl() {}

	@Override
	public <T extends DomainObjectEvent> void publish(T event) {
		requireNonNull(event);
		subscribers.stream().filter(subscribedToEventType(event.getClass())).forEach(subscriber -> {
			@SuppressWarnings("unchecked")
			val castedSubscriber = (DomainObjectEventSubscriber<T>) subscriber;
			castedSubscriber.handle(event);
		});
	}

	private Predicate<DomainObjectEventSubscriber<?>> subscribedToEventType(Class<?> eventType) {
		return subscriber -> subscriber.subscribedToEventType().isAssignableFrom(eventType);
	}

	@Override
	public <T extends DomainObjectEvent> void subscribe(DomainObjectEventSubscriber<T> subscriber) {
		subscribers.add(requireNonNull(subscriber));
	}
}
