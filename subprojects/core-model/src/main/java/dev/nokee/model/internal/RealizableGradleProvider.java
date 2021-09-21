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
import lombok.EqualsAndHashCode;
import lombok.val;
import org.gradle.api.provider.Provider;

@EqualsAndHashCode
public final class RealizableGradleProvider implements RealizableDomainObject {
	private final DomainObjectIdentifier identifier;
	private final Provider<?> provider;
	@EqualsAndHashCode.Exclude private final DomainObjectEventPublisher eventPublisher;

	public RealizableGradleProvider(DomainObjectIdentifier identifier, Provider<?> provider, DomainObjectEventPublisher eventPublisher) {
		assert provider != null;
		this.identifier = identifier;
		this.provider = provider;
		this.eventPublisher = eventPublisher;
	}

	@Override
	public void realize() {
		val value = provider.get();
		eventPublisher.publish(new DomainObjectCreated<>(identifier, value));
	}
}
