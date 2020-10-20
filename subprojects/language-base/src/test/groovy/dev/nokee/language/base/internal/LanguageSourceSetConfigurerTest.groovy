package dev.nokee.language.base.internal

import dev.nokee.language.base.LanguageSourceSet
import dev.nokee.model.internal.AbstractDomainObjectConfigurerTest
import dev.nokee.model.internal.DomainObjectConfigurer
import spock.lang.Subject

@Subject(LanguageSourceSetConfigurer)
class LanguageSourceSetConfigurerTest extends AbstractDomainObjectConfigurerTest<LanguageSourceSet> implements LanguageSourceSetFixture {
	@Override
	protected DomainObjectConfigurer<LanguageSourceSet> newSubject() {
		return new LanguageSourceSetConfigurer(eventPublisher)
	}
}
