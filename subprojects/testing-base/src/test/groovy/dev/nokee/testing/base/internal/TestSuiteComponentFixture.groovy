package dev.nokee.testing.base.internal

import dev.nokee.model.DomainObjectIdentifier
import dev.nokee.model.internal.*
import dev.nokee.platform.base.Component
import dev.nokee.platform.base.internal.ComponentIdentifier
import dev.nokee.platform.base.internal.ComponentName
import dev.nokee.platform.base.internal.components.*
import dev.nokee.testing.base.TestSuiteComponent
import org.apache.commons.lang3.RandomStringUtils

trait TestSuiteComponentFixture {
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

	PolymorphicDomainObjectInstantiator<Component> newEntityInstantiator() {
		return new ComponentInstantiator('test instantiator')
	}

	Class<Component> getEntityType() {
		return Component
	}

	Class<? extends Component> getEntityImplementationType() {
		return TestSuiteComponentImpl
	}

	def <S extends TestSuiteComponent> TypeAwareDomainObjectIdentifier<S> entityIdentifier(Class<S> type, DomainObjectIdentifier owner) {
		return ComponentIdentifier.of(ComponentName.of('a' + RandomStringUtils.randomAlphanumeric(12)), type, (ProjectIdentifier)owner)
	}

    DomainObjectIdentifier ownerIdentifier(String name) {
		return ProjectIdentifier.of(name)
	}

	Class<? extends TestSuiteComponent> getMyEntityType() {
		return MyTestSuiteComponent
	}

	Class<? extends TestSuiteComponent> getMyEntityChildType() {
		return MyTestSuiteChildComponent
	}

	static class MyTestSuiteComponent implements TestSuiteComponent {
		@Override
		TestSuiteComponent testedComponent(Object component) {
			throw new UnsupportedOperationException()
		}
	}
	static class MyTestSuiteChildComponent extends MyTestSuiteComponent {}
	static class TestSuiteComponentImpl implements TestSuiteComponent {
		@Override
		TestSuiteComponent testedComponent(Object component) {
			throw new UnsupportedOperationException()
		}
	}
}
