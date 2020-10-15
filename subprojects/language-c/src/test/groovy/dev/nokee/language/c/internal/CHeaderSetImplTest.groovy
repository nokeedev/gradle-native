package dev.nokee.language.c.internal

import dev.nokee.language.base.AbstractLanguageSourceSetTest
import spock.lang.Subject

@Subject(CHeaderSetImpl)
class CHeaderSetImplTest extends AbstractLanguageSourceSetTest<dev.nokee.language.c.CHeaderSet> {
	@Override
	protected dev.nokee.language.c.CHeaderSet newSubject() {
		return new CHeaderSetImpl(project.objects)
	}

	@Override
	protected Class<dev.nokee.language.c.CHeaderSet> getPublicType() {
		return dev.nokee.language.c.CHeaderSet
	}

	@Override
	protected Set<String> getDefaultFilterIncludes() {
		return ['**/*.h']
	}

	@Override
	protected String fileName(String fileName) {
		return fileName + '.h'
	}
}
