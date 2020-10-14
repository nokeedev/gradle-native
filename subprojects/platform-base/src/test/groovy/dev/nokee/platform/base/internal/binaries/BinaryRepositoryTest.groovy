package dev.nokee.platform.base.internal.binaries

import dev.nokee.model.internal.AbstractRealizableDomainObjectRepositoryTest
import dev.nokee.model.internal.RealizableDomainObjectRealizer
import dev.nokee.model.internal.RealizableDomainObjectRepository
import dev.nokee.platform.base.Binary
import spock.lang.Subject

@Subject(BinaryRepository)
class BinaryRepositoryTest extends AbstractRealizableDomainObjectRepositoryTest<Binary> implements BinaryFixture {
	@Override
	protected RealizableDomainObjectRepository<Binary> newSubject(RealizableDomainObjectRealizer realizer) {
		return new BinaryRepository(eventPublisher, realizer, providerFactory)
	}
}
