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
