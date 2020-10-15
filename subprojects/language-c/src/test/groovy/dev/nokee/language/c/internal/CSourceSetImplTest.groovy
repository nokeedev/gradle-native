package dev.nokee.language.c.internal

import dev.nokee.language.base.AbstractLanguageSourceSetTest
import spock.lang.Subject

@Subject(CSourceSetImpl)
class CSourceSetImplTest extends AbstractLanguageSourceSetTest<dev.nokee.language.c.CSourceSet> {
	@Override
	protected dev.nokee.language.c.CSourceSet newSubject() {
		return new CSourceSetImpl(project.objects)
	}

	@Override
	protected Class<dev.nokee.language.c.CSourceSet> getPublicType() {
		return dev.nokee.language.c.CSourceSet
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
