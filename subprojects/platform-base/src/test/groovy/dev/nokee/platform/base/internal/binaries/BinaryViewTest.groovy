package dev.nokee.platform.base.internal.binaries

import dev.nokee.model.internal.AbstractDomainObjectViewTest
import dev.nokee.platform.base.Binary
import spock.lang.Subject

@Subject(BinaryViewImpl)
class BinaryViewTest extends AbstractDomainObjectViewTest<Binary> implements BinaryFixture {
}
