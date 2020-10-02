package dev.nokee.model.internal;

public interface DomainObjectEventSubscriber<T extends DomainObjectEvent> {
	void handle(T event);

	Class<? extends T> subscribedToEventType();
}
