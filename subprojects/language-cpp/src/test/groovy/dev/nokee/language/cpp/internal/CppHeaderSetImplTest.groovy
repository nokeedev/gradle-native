package dev.nokee.language.cpp.internal

import dev.nokee.language.base.AbstractLanguageSourceSetTest
import spock.lang.Subject

@Subject(CppHeaderSetImpl)
class CppHeaderSetImplTest extends AbstractLanguageSourceSetTest<dev.nokee.language.cpp.CppHeaderSet> {
	@Override
	protected dev.nokee.language.cpp.CppHeaderSet newSubject() {
		return new CppHeaderSetImpl(project.objects)
	}

	@Override
	protected Class<dev.nokee.language.cpp.CppHeaderSet> getPublicType() {
		return dev.nokee.language.cpp.CppHeaderSet
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
