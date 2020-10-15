package dev.nokee.language.cpp.internal

import dev.nokee.language.base.AbstractLanguageSourceSetTest
import dev.nokee.language.cpp.CppHeaderSet
import spock.lang.Subject

@Subject(CppHeaderSetImpl)
class CppHeaderSetImplTest extends AbstractLanguageSourceSetTest<CppHeaderSet> {
	@Override
	protected CppHeaderSet newSubject() {
		return new CppHeaderSetImpl(newIdentifier(), project.objects)
	}

	@Override
	protected Class<CppHeaderSet> getPublicType() {
		return CppHeaderSet
	}

	@Override
	protected Set<String> getDefaultFilterIncludes() {
		return ['**/*.hpp', '**/*.h++', '**/*.hxx']
	}

	@Override
	protected String fileName(String fileName) {
		return fileName + '.hpp'
	}
}
