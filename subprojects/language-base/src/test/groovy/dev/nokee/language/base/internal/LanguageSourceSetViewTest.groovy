package dev.nokee.language.base.internal

import dev.nokee.language.base.LanguageSourceSet
import dev.nokee.model.internal.AbstractDomainObjectViewTest
import spock.lang.Subject

@Subject(LanguageSourceSetViewImpl)
class LanguageSourceSetViewTest extends AbstractDomainObjectViewTest<LanguageSourceSet> implements LanguageSourceSetFixture {
}
