package dev.nokee.language.base.internal

import dev.nokee.language.base.LanguageSourceSet
import dev.nokee.model.internal.AbstractRealizableDomainObjectRepositoryTest
import dev.nokee.model.internal.RealizableDomainObjectRealizer
import dev.nokee.model.internal.RealizableDomainObjectRepository
import spock.lang.Subject

@Subject(LanguageSourceSetRepository)
class LanguageSourceSetRepositoryTest extends AbstractRealizableDomainObjectRepositoryTest<LanguageSourceSet> implements LanguageSourceSetFixture {
	@Override
	protected RealizableDomainObjectRepository<LanguageSourceSet> newSubject(RealizableDomainObjectRealizer realizer) {
		return new LanguageSourceSetRepository(eventPublisher, realizer, providerFactory)
	}
}
