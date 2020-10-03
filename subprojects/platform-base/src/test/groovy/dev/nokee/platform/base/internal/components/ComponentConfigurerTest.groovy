package dev.nokee.platform.base.internal.components


import dev.nokee.model.internal.AbstractDomainObjectConfigurerTest
import dev.nokee.model.internal.DomainObjectConfigurer
import dev.nokee.platform.base.Component
import spock.lang.Subject

@Subject(ComponentConfigurer)
class ComponentConfigurerTest extends AbstractDomainObjectConfigurerTest<Component> implements ComponentFixture {
	@Override
	protected DomainObjectConfigurer<Component> newSubject() {
		return new ComponentConfigurer(eventPublisher)
	}
}
