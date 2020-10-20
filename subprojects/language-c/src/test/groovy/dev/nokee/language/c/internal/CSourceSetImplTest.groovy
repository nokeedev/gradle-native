package dev.nokee.language.c.internal

import dev.nokee.language.base.AbstractLanguageSourceSetTest
import dev.nokee.language.c.CSourceSet
import spock.lang.Subject

@Subject(CSourceSetImpl)
class CSourceSetImplTest extends AbstractLanguageSourceSetTest<CSourceSet> {
	@Override
	protected CSourceSet newSubject() {
		return new CSourceSetImpl(newIdentifier(), project.objects)
	}

	@Override
	protected Class<CSourceSet> getPublicType() {
		return CSourceSet
	}

	@Override
	protected Set<String> getDefaultFilterIncludes() {
		return ['**/*.c']
	}

	@Override
	protected String fileName(String fileName) {
		return fileName + '.c'
	}
}
