package dev.nokee.language.base.internal

import dev.nokee.language.base.LanguageSourceSet
import dev.nokee.model.internal.AbstractKnownDomainObjectTest
import spock.lang.Subject

@Subject(KnownLanguageSourceSet)
class KnownLanguageSourceSetTest extends AbstractKnownDomainObjectTest<LanguageSourceSet> implements LanguageSourceSetFixture {
}
