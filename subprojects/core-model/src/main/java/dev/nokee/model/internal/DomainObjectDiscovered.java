package dev.nokee.model.internal;

import lombok.Value;
import org.gradle.api.Action;
import org.gradle.api.reflect.TypeOf;

@Value
public class DomainObjectDiscovered<T> implements DomainObjectEvent {
	TypeAwareDomainObjectIdentifier<? extends T> identifier;

	public Class<?> getType() {
		return identifier.getType();
	}

	public static <S> DomainObjectEventSubscriber<DomainObjectDiscovered<S>> discoveredType(Class<S> entityType, Action<? super TypeAwareDomainObjectIdentifier<? extends S>> action) {
		return new DiscoveredTypeSubscriberRule<>(entityType, action);
	}

	private static final class DiscoveredTypeSubscriberRule<T> implements   DomainObjectEventSubscriber<DomainObjectDiscovered<T>> {
		private final Class<T> entityType;
		private final Action<? super TypeAwareDomainObjectIdentifier<? extends T>> action;

		DiscoveredTypeSubscriberRule(Class<T> entityType, Action<? super TypeAwareDomainObjectIdentifier<? extends T>> action) {
			this.entityType = entityType;
			this.action = action;
		}

		@Override
		public void handle(DomainObjectDiscovered<T> event) {
			if (entityType.isAssignableFrom(event.getIdentifier().getType())) {
				action.execute(event.getIdentifier());
			}
		}

		@Override
		public Class<? extends DomainObjectDiscovered<T>> subscribedToEventType() {
			return new TypeOf<DomainObjectDiscovered<T>>() {}.getConcreteClass();
		}

		@Override
		public String toString() {
			return "DomainObjectDiscovered.discoveredType(" + entityType.getCanonicalName() + ", " + action.toString() + ")";
		}
	}
}
