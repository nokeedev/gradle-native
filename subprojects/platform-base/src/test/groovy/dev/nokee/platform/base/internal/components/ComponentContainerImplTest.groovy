package dev.nokee.platform.base.internal.components

import dev.nokee.model.DomainObjectIdentifier
import dev.nokee.model.internal.AbstractDomainObjectContainer
import dev.nokee.model.internal.AbstractDomainObjectContainerTest
import dev.nokee.model.internal.TypeAwareDomainObjectIdentifier
import dev.nokee.platform.base.Component
import dev.nokee.platform.base.internal.ComponentIdentifier
import dev.nokee.platform.base.internal.ComponentName
import spock.lang.Subject

@Subject(ComponentContainerImpl)
class ComponentContainerImplTest extends AbstractDomainObjectContainerTest<Component, Component> implements ComponentFixture {
	@Override
	protected AbstractDomainObjectContainer<Component, Component> newSubject() {
		return new ComponentContainerImpl(ownerIdentifier, entityConfigurer, eventPublisher, newEntityProviderFactory(), entityRepository, newEntityFactory(), newEntityInstantiator())
	}

	@Override
	protected TypeAwareDomainObjectIdentifier entityIdentifier(String name, Class entityType, DomainObjectIdentifier ownerIdentifier) {
		return ComponentIdentifier.of(ComponentName.of(name), entityType, ownerIdentifier)
	}

	@Override
	protected Class<? extends Component> getUnknownEntityType() {
		return UnknownComponent
	}

	interface UnknownComponent extends Component {}
}
