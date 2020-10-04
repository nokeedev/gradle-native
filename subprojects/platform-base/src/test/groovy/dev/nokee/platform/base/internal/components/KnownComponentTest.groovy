package dev.nokee.platform.base.internal.components

import dev.nokee.model.internal.AbstractKnownDomainObjectTest
import dev.nokee.platform.base.Component
import spock.lang.Subject

@Subject(KnownComponent)
class KnownComponentTest extends AbstractKnownDomainObjectTest<Component> implements ComponentFixture {}
