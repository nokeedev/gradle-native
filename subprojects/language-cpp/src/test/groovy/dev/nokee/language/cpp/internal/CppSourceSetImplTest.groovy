package dev.nokee.language.cpp.internal

import dev.nokee.language.base.AbstractLanguageSourceSetTest
import dev.nokee.language.cpp.CppSourceSet
import spock.lang.Subject

@Subject(CppSourceSetImpl)
class CppSourceSetImplTest extends AbstractLanguageSourceSetTest<CppSourceSet> {
	@Override
	protected CppSourceSet newSubject() {
		return new CppSourceSetImpl(newIdentifier(), project.objects)
	}

	@Override
	protected Class<CppSourceSet> getPublicType() {
		return CppSourceSet
	}

	@Override
	protected Set<String> getDefaultFilterIncludes() {
		return ['**/*.cp', '**/*.cpp', '**/*.c++', '**/*.cc', '**/*.cxx']
	}

	@Override
	protected String fileName(String fileName) {
		return fileName + '.cpp'
	}
}
