package dev.nokee.platform.base.internal.variants

import dev.nokee.model.internal.AbstractKnownDomainObjectTest
import dev.nokee.platform.base.Variant
import spock.lang.Subject

@Subject(KnownVariant)
class KnownVariantTest extends AbstractKnownDomainObjectTest<Variant> implements VariantFixture {
}
