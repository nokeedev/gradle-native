package dev.nokee.platform.nativebase.internal.rules;

import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.internal.*;

public interface NokeeEntitiesFixture {
	default DomainObjectEventPublisher getEventPublisher() {
		return TestStates.getState(this).getEventPublisher();
	}

	default RealizableDomainObjectRealizer getEntityRealizer() {
		return TestStates.getState(this).getEntityRealizer();
	}

	default <T, S extends TypeAwareDomainObjectIdentifier<T>> S discovered(S identifier) {
		getEventPublisher().publish(new DomainObjectDiscovered<>(identifier));
		return identifier;
	}

	default <S> CreatedEntity<S> created(TypeAwareDomainObjectIdentifier<S> identifier, S entity) {
		getEventPublisher().publish(new DomainObjectCreated<>(identifier, entity));
		return new CreatedEntity<>(identifier, entity);
	}

	final class CreatedEntity<T> {
		final DomainObjectIdentifier identifier;
		final T entity;

		private CreatedEntity(DomainObjectIdentifier identifier, T entity) {
			this.identifier = identifier;
			this.entity = entity;
		}

		// for groovy object destructuring
		Object getAt(int idx) {
			if (idx == 0) return identifier;
			else if (idx == 1) return entity;
			else throw new RuntimeException("Wrong index, use 0 or 1");
		}
	}
}
