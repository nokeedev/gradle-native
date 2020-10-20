package dev.nokee.language.c.internal

import dev.nokee.language.base.AbstractLanguageSourceSetTest
import dev.nokee.language.c.CHeaderSet
import spock.lang.Subject

@Subject(CHeaderSetImpl)
class CHeaderSetImplTest extends AbstractLanguageSourceSetTest<CHeaderSet> {
	@Override
	protected CHeaderSet newSubject() {
		return new CHeaderSetImpl(newIdentifier(), project.objects)
	}

	@Override
	protected Class<CHeaderSet> getPublicType() {
		return CHeaderSet
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
