package dev.nokee.platform.base.internal.components

import dev.nokee.model.internal.AbstractRealizableDomainObjectRepositoryTest
import dev.nokee.model.internal.RealizableDomainObjectRealizer
import dev.nokee.model.internal.RealizableDomainObjectRepository
import dev.nokee.platform.base.Component
import spock.lang.Subject

@Subject(ComponentRepository)
class ComponentRepositoryTest extends AbstractRealizableDomainObjectRepositoryTest<Component> implements ComponentFixture {
	@Override
	protected RealizableDomainObjectRepository<Component> newSubject(RealizableDomainObjectRealizer realizer) {
		return new ComponentRepository(eventPublisher, realizer, providerFactory)
	}
}
