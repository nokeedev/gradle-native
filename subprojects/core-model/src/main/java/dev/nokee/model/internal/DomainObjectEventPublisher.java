package dev.nokee.model.internal;

public interface DomainObjectEventPublisher {
	<T extends DomainObjectEvent> void publish(T event);

	<T extends DomainObjectEvent> void subscribe(DomainObjectEventSubscriber<T> subscriber);
}
