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
import lombok.val;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.reflect.TypeOf;

import java.util.*;

public final class RealizableDomainObjectRealizerImpl implements RealizableDomainObjectRealizer {
	private static final Logger LOGGER = Logging.getLogger(RealizableDomainObjectRealizerImpl.class);
	private final Set<DomainObjectIdentifier> knownIdentifiers = new HashSet<>();
	private final Map<DomainObjectIdentifier, RealizableDomainObject> identifierToRealizable = new HashMap<>();
	private final Map<DomainObjectIdentifier, Object> identifierToCreated = new HashMap<>();
	private final Set<DomainObjectIdentifier> realizedIdentifiers = new HashSet<>();
	private final DomainObjectEventPublisher eventPublisher;

	public RealizableDomainObjectRealizerImpl(DomainObjectEventPublisher eventPublisher) {
		this.eventPublisher = eventPublisher;
		eventPublisher.subscribe(new DomainObjectEventSubscriber<RealizableDomainObjectDiscovered>() {
			private final boolean forceRealize = Boolean.parseBoolean(System.getProperty("dev.nokee.internal.realize.force", "false"));

			@Override
			public void handle(RealizableDomainObjectDiscovered event) {
				knownIdentifiers.add(event.getIdentifier());
				if (forceRealize) {
					event.getObject().realize();
				} else {
					identifierToRealizable.put(event.getIdentifier(), event.getObject());
				}
			}

			@Override
			public Class<RealizableDomainObjectDiscovered> subscribedToEventType() {
				return RealizableDomainObjectDiscovered.class;
			}
		});
		eventPublisher.subscribe(new DomainObjectEventSubscriber<DomainObjectCreated<Object>>() {
			@Override
			public void handle(DomainObjectCreated<Object> event) {
				knownIdentifiers.add(event.getIdentifier());
				identifierToCreated.put(event.getIdentifier(), event.getObject());
			}

			@Override
			public Class<DomainObjectCreated<Object>> subscribedToEventType() {
				return new TypeOf<DomainObjectCreated<Object>>() {}.getConcreteClass();
			}
		});
	}

	private void realize(DomainObjectIdentifier identifier) {
		if (realizedIdentifiers.contains(identifier)) {
			return;
		}

		val resolveChain = new ArrayDeque<DomainObjectIdentifier>();
		resolveChain.push(identifier);
		DomainObjectIdentifierInternal currentIdentifier = (DomainObjectIdentifierInternal) identifier;
		while (currentIdentifier.getParentIdentifier().isPresent()) {
			currentIdentifier = currentIdentifier.getParentIdentifier().get();
			resolveChain.push(currentIdentifier);
		}

		while (!resolveChain.isEmpty()) {
			val resolvingIdentifier = resolveChain.pop();
			val elementToRealize = identifierToRealizable.remove(resolvingIdentifier);
			if (elementToRealize != null) {
				elementToRealize.realize();
			} else {
				LOGGER.debug("Element " + resolvingIdentifier + "is not known.");
			}
		}

		if (knownIdentifiers.contains(identifier)) {
			val realizedElement = identifierToCreated.remove(identifier);
			if (realizedElement == null) {
				throw new IllegalStateException("Element wasn't created");
			}
			eventPublisher.publish(new DomainObjectRealized<>(identifier, realizedElement));
		}

		realizedIdentifiers.add(identifier);
	}

	/**
	 * Returns a callable instance which realize the specified identifier and return an empty list.
	 * This is quite useful when the specified identifier needs to be realized iff a task participate in the task graph.
	 * It's a sort-of old school software model-ish behavior.
	 *
	 * @param identifier the identifier to realize
	 * @param <T> the expected list type to return
	 * @return a callable instance which realize the specified identifier and return empty list upon calling.
	 */
	public <T extends DomainObjectIdentifier> T ofElement(T identifier) {
		realize(identifier);
		return identifier;
	}
}
