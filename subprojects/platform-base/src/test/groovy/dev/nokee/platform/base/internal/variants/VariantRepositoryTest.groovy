package dev.nokee.platform.base.internal.variants

import dev.nokee.model.internal.AbstractRealizableDomainObjectRepositoryTest
import dev.nokee.model.internal.RealizableDomainObjectRealizer
import dev.nokee.model.internal.RealizableDomainObjectRepository
import dev.nokee.platform.base.Variant

class VariantRepositoryTest extends AbstractRealizableDomainObjectRepositoryTest<Variant> implements VariantFixture {
	@Override
	protected RealizableDomainObjectRepository<Variant> newSubject(RealizableDomainObjectRealizer realizer) {
		return new VariantRepository(eventPublisher, realizer, providerFactory)
	}
}
