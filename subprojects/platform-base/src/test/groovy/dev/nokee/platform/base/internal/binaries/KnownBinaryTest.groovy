package dev.nokee.platform.base.internal.binaries

import dev.nokee.model.internal.AbstractKnownDomainObjectTest
import dev.nokee.platform.base.Binary
import spock.lang.Subject

@Subject(KnownBinary)
class KnownBinaryTest extends AbstractKnownDomainObjectTest<Binary> implements BinaryFixture {
}
