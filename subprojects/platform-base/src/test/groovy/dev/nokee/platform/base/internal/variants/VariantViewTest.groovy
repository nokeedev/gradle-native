package dev.nokee.platform.base.internal.variants

import dev.nokee.model.internal.AbstractDomainObjectViewTest
import dev.nokee.platform.base.Variant
import spock.lang.Subject

@Subject(VariantViewImpl)
class VariantViewTest extends AbstractDomainObjectViewTest<Variant> implements VariantFixture {
}
