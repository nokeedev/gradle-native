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
