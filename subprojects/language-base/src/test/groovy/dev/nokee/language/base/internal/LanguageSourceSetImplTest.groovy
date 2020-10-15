package dev.nokee.language.base.internal

import dev.nokee.language.base.AbstractLanguageSourceSetTest
import dev.nokee.language.base.LanguageSourceSet
import dev.nokee.model.internal.DomainObjectIdentifierInternal
import spock.lang.Subject

@Subject(LanguageSourceSetImpl)
class LanguageSourceSetImplTest extends AbstractLanguageSourceSetTest<LanguageSourceSet> {
	@Override
	protected LanguageSourceSet newSubject() {
		return new LanguageSourceSetImpl(project.objects)
	}

	@Override
	protected Class<LanguageSourceSet> getPublicType() {
		return LanguageSourceSet
	}

	@Override
	protected Set<String> getDefaultFilterIncludes() {
		return []
	}
}
