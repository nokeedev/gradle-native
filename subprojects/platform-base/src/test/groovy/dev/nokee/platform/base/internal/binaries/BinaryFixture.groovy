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
package dev.nokee.platform.base.internal.binaries

import dev.nokee.model.DomainObjectIdentifier
import dev.nokee.model.internal.*
import dev.nokee.platform.base.Binary
import dev.nokee.platform.base.Component
import dev.nokee.platform.base.internal.*
import org.apache.commons.lang3.RandomStringUtils

trait BinaryFixture {
	RealizableDomainObjectRepository<Binary> newEntityRepository() {
		def realizer = new RealizableDomainObjectRealizerImpl(eventPublisher)
		return new BinaryRepository(eventPublisher, realizer, providerFactory)
	}

    DomainObjectConfigurer<Binary> newEntityConfigurer() {
		return new BinaryConfigurer(eventPublisher)
	}

    KnownDomainObjectFactory<Binary> newEntityFactory() {
		return new KnownBinaryFactory({ entityRepository }, { entityConfigurer })
	}

    DomainObjectViewFactory<Binary> newEntityViewFactory() {
		return new BinaryViewFactory(entityRepository, entityConfigurer)
	}

	DomainObjectProviderFactory<Binary> newEntityProviderFactory() {
		throw new UnsupportedOperationException()
	}

	Class<Binary> getEntityType() {
		return Binary
	}

	Class<? extends Binary> getEntityImplementationType() {
		return BinaryImpl
	}

	def <S extends Binary> TypeAwareDomainObjectIdentifier<S> entityIdentifier(Class<S> type, DomainObjectIdentifier owner) {
		return BinaryIdentifier.of(BinaryName.of('a' + RandomStringUtils.randomAlphanumeric(12)), type, owner)
	}

    DomainObjectIdentifier ownerIdentifier(String name) {
		return ComponentIdentifier.of(ComponentName.of(name), Component, ProjectIdentifier.of('root'))
	}

	Class<? extends Binary> getMyEntityType() {
		return MyBinary
	}

	static class MyBinary implements Binary {}

	Class<? extends Binary> getMyEntityChildType() {
		return MyBinaryChild
	}

	static class MyBinaryChild extends MyBinary {}

	static class BinaryImpl implements Binary {}
}
