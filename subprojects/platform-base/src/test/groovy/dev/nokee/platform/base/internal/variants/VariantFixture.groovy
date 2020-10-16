package dev.nokee.platform.base.internal.variants

import dev.nokee.model.DomainObjectIdentifier
import dev.nokee.model.internal.*
import dev.nokee.platform.base.*
import dev.nokee.platform.base.internal.ComponentIdentifier
import dev.nokee.platform.base.internal.ComponentName
import dev.nokee.platform.base.internal.VariantIdentifier
import org.apache.commons.lang3.RandomStringUtils
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
		return ComponentIdentifier.of(ComponentName.of(name), Component, ProjectIdentifier.of('root'))
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
		Provider<Binary> getDevelopmentBinary() {
			throw new UnsupportedOperationException()
		}

		@Override
        BuildVariant getBuildVariant() {
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
		Provider<Binary> getDevelopmentBinary() {
			throw new UnsupportedOperationException()
		}

		@Override
        BuildVariant getBuildVariant() {
			throw new UnsupportedOperationException()
		}
	}
}
