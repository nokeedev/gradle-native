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
package dev.nokee.platform.base.internal.variants

import dev.nokee.model.DomainObjectIdentifier
import dev.nokee.model.internal.*
import dev.nokee.platform.base.Binary
import dev.nokee.platform.base.BinaryView
import dev.nokee.platform.base.BuildVariant
import dev.nokee.platform.base.Variant
import dev.nokee.platform.base.internal.ComponentIdentifier
import dev.nokee.platform.base.internal.ComponentName
import dev.nokee.platform.base.internal.VariantIdentifier
import org.apache.commons.lang3.RandomStringUtils
import org.gradle.api.Action
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider

trait VariantFixture {
	RealizableDomainObjectRepository<Variant> newEntityRepository() {
		def realizer = new RealizableDomainObjectRealizerImpl(eventPublisher)
		return new VariantRepository(eventPublisher, realizer, providerFactory)
	}

    DomainObjectConfigurer<Variant> newEntityConfigurer() {
		return new VariantConfigurer(eventPublisher)
	}

    KnownDomainObjectFactory<Variant> newEntityFactory() {
		return new KnownVariantFactory({ entityRepository }, { entityConfigurer })
	}

    DomainObjectViewFactory<Variant> newEntityViewFactory() {
		return new VariantViewFactory(entityRepository, entityConfigurer, entityFactory)
	}

	DomainObjectProviderFactory<Variant> newEntityProviderFactory() {
		return null
	}

	Class<Variant> getEntityType() {
		return Variant
	}

	Class<? extends Variant> getEntityImplementationType() {
		return VariantImpl
	}

	def <S extends Variant> TypeAwareDomainObjectIdentifier<S> entityIdentifier(Class<S> type, DomainObjectIdentifier owner) {
		return VariantIdentifier.of('a' + RandomStringUtils.randomAlphanumeric(12), type, (ComponentIdentifier)owner)
	}

    DomainObjectIdentifier ownerIdentifier(String name) {
		return ComponentIdentifier.of(ComponentName.of(name), ProjectIdentifier.of('root'))
	}

	Class<? extends Variant> getMyEntityType() {
		return MyVariant
	}

	static class MyVariant implements Variant {
		@Override
        BinaryView<Binary> getBinaries() {
			throw new UnsupportedOperationException()
		}

		@Override
		void binaries(Action<? super BinaryView<Binary>> action) {
			throw new UnsupportedOperationException()
		}

		@Override
		void binaries(Closure closure) {
			throw new UnsupportedOperationException()
		}

		@Override
		Property<Binary> getDevelopmentBinary() {
			throw new UnsupportedOperationException()
		}

		@Override
        BuildVariant getBuildVariant() {
			throw new UnsupportedOperationException()
		}

		@Override
		String getName() {
			throw new UnsupportedOperationException()
		}
	}

	Class<? extends Variant> getMyEntityChildType() {
		return MyVariantChild
	}

	static class MyVariantChild extends MyVariant {}
	static class VariantImpl implements Variant {
		@Override
        BinaryView<Binary> getBinaries() {
			throw new UnsupportedOperationException()
		}

		@Override
		void binaries(Action<? super BinaryView<Binary>> action) {
			throw new UnsupportedOperationException()
		}

		@Override
		void binaries(Closure closure) {
			throw new UnsupportedOperationException()
		}

		@Override
		Property<Binary> getDevelopmentBinary() {
			throw new UnsupportedOperationException()
		}

		@Override
        BuildVariant getBuildVariant() {
			throw new UnsupportedOperationException()
		}

		@Override
		String getName() {
			throw new UnsupportedOperationException()
		}
	}
}
