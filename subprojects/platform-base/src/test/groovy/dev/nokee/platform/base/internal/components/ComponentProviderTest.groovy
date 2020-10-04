package dev.nokee.platform.base.internal.components

import dev.nokee.model.internal.AbstractDomainObjectProviderTest
import dev.nokee.platform.base.Component
import spock.lang.Subject

@Subject(ComponentProvider)
class ComponentProviderTest extends AbstractDomainObjectProviderTest<Component> implements ComponentFixture {}
