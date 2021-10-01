/*
 * Copyright 2020-2021 the original author or authors.
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
package dev.nokee.model.internal

import dev.nokee.internal.testing.util.ProjectTestUtils
import dev.nokee.model.DomainObjectIdentifier
import org.apache.commons.lang3.RandomStringUtils
import spock.lang.Shared
import spock.lang.Specification

import java.util.function.Supplier

abstract class DomainObjectSpec<T> extends Specification {
	@Shared def providerFactory = ProjectTestUtils.providerFactory()
	def eventPublisher = new DomainObjectEventPublisherImpl()
	def entityRepository = newEntityRepository()
	def entityConfigurer = newEntityConfigurer()
	def entityFactory = newEntityFactory()

	protected abstract RealizableDomainObjectRepository<T> newEntityRepository()

	protected abstract DomainObjectConfigurer<T> newEntityConfigurer()

	protected abstract KnownDomainObjectFactory<T> newEntityFactory()

	protected abstract DomainObjectViewFactory<T> newEntityViewFactory()

	protected abstract DomainObjectProviderFactory<T> newEntityProviderFactory()

	protected abstract Class<T> getEntityType()

	protected abstract Class<? extends T> getMyEntityType()

	protected abstract Class<? extends T> getEntityImplementationType()

	protected TypeAwareDomainObjectIdentifier<T> entityIdentifier(DomainObjectIdentifier owner) {
		return entityIdentifier(entityImplementationType, owner)
	}

	protected abstract <S extends T> TypeAwareDomainObjectIdentifier<S> entityIdentifier(Class<S> type, DomainObjectIdentifier owner)

	protected <S extends T> List entityCreated(DomainObjectIdentifier identifier, Supplier<S> entity) {
		eventPublisher.publish(new DomainObjectCreated<>(identifier, entity.get()))
		return [identifier, entity.get()]
	}

	protected List entityCreated(List packed) {
		return entityCreated((DomainObjectIdentifier)packed[0], (Supplier)packed[1])
	}

	protected <S extends T> TypeAwareDomainObjectIdentifier<S> entityDiscovered(TypeAwareDomainObjectIdentifier<S> identifier) {
		eventPublisher.publish(new DomainObjectDiscovered<>(identifier))
		return identifier
	}

	protected <S extends T> List entityRealized(TypeAwareDomainObjectIdentifier<S> identifier, S entity) {
		eventPublisher.publish(new DomainObjectRealized<>(identifier, entity))
		return [identifier, entity]
	}

	protected List entityRealized(List packed) {
		return entityRealized((TypeAwareDomainObjectIdentifier)packed[0], (T)packed[1])
	}

	protected <S extends T> List entity(TypeAwareDomainObjectIdentifier<S> identifier) {
		def result = new EntitySupplier<>(identifier)
		return [identifier, result]
	}

	private static class EntitySupplier<T> implements Supplier<T> {
		private final TypeAwareDomainObjectIdentifier<T> identifier
		private T value = null

		EntitySupplier(TypeAwareDomainObjectIdentifier<T> identifier) {
			this.identifier = identifier
		}

		@Override
		T get() {
			if (value == null) {
				value = identifier.type.newInstance()
			}
			return value
		}
	}

	protected <S extends T> KnownDomainObject<S> knownEntity(TypeAwareDomainObjectIdentifier<S> identifier) {
		return entityFactory.create(identifier)
	}

	protected DomainObjectIdentifier getOwnerIdentifier() {
		return ownerIdentifier('main')
	}

	protected static String newLowerCamelRandomString() {
		return 'a' + RandomStringUtils.randomAlphanumeric(12)
	}

	protected abstract DomainObjectIdentifier ownerIdentifier(String name = newLowerCamelRandomString());
}
