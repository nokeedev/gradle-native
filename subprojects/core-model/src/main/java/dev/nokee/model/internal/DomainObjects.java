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
