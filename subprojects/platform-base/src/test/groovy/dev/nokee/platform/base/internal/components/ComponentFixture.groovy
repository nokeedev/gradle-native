package dev.nokee.platform.base.internal.components

import dev.nokee.model.DomainObjectIdentifier
import dev.nokee.model.internal.*
import dev.nokee.platform.base.Component
import dev.nokee.platform.base.internal.ComponentIdentifier
import dev.nokee.platform.base.internal.ComponentName
import dev.nokee.platform.base.internal.ProjectIdentifier
import org.apache.commons.lang3.RandomStringUtils

trait ComponentFixture {
	RealizableDomainObjectRepository<Component> newEntityRepository() {
		def realizer = new RealizableDomainObjectRealizerImpl(eventPublisher)
		return new ComponentRepository(eventPublisher, realizer, providerFactory)
	}

    DomainObjectConfigurer<Component> newEntityConfigurer() {
		return new ComponentConfigurer(eventPublisher)
	}

    KnownDomainObjectFactory<Component> newEntityFactory() {
		return new KnownComponentFactory({ entityRepository }, { entityConfigurer })
	}

    DomainObjectViewFactory<Component> newEntityViewFactory() {
		throw new UnsupportedOperationException()
	}

	DomainObjectProviderFactory<Component> newEntityProviderFactory() {
		return new ComponentProviderFactory(entityRepository, entityConfigurer)
	}

	Class<Component> getEntityType() {
		return Component
	}

	Class<? extends Component> getEntityImplementationType() {
		return ComponentImpl
	}

	def <S extends Component> TypeAwareDomainObjectIdentifier<S> entityIdentifier(Class<S> type, DomainObjectIdentifier owner) {
		return ComponentIdentifier.of(ComponentName.of('a' + RandomStringUtils.randomAlphanumeric(12)), type, (ProjectIdentifier)owner)
	}

    DomainObjectIdentifier ownerIdentifier(String name) {
		return ProjectIdentifier.of(name)
	}

	Class<? extends Component> getMyEntityType() {
		return MyComponent
	}

	static class MyComponent implements Component {}
	static class ComponentImpl implements Component {}
}
