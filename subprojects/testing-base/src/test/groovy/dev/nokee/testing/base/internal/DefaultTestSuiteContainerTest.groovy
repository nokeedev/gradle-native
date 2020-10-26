package dev.nokee.testing.base.internal

import dev.nokee.model.DomainObjectIdentifier
import dev.nokee.model.internal.AbstractDomainObjectContainer
import dev.nokee.model.internal.AbstractDomainObjectContainerTest
import dev.nokee.model.internal.TypeAwareDomainObjectIdentifier
import dev.nokee.platform.base.Component
import dev.nokee.platform.base.internal.ComponentIdentifier
import dev.nokee.platform.base.internal.ComponentName
import dev.nokee.testing.base.TestSuiteComponent
import spock.lang.Subject

@Subject(DefaultTestSuiteContainer)
class DefaultTestSuiteContainerTest extends AbstractDomainObjectContainerTest<Component, TestSuiteComponent> implements TestSuiteComponentFixture {

	@Override
	protected AbstractDomainObjectContainer<Component, TestSuiteComponent> newSubject() {
		return new DefaultTestSuiteContainer(ownerIdentifier, entityConfigurer, eventPublisher, newEntityProviderFactory(), entityRepository, newEntityFactory(), newEntityInstantiator())
	}

	@Override
	protected TypeAwareDomainObjectIdentifier entityIdentifier(String name, Class entityType, DomainObjectIdentifier ownerIdentifier) {
		return ComponentIdentifier.of(ComponentName.of(name), entityType, ownerIdentifier)
	}

	@Override
	protected Class getUnknownEntityType() {
		return UnknownComponent
	}

	interface UnknownComponent extends Component {}
}
