package dev.nokee.language.cpp.internal

import dev.nokee.language.base.AbstractLanguageSourceSetTest
import spock.lang.Subject

@Subject(CppSourceSetImpl)
class CppSourceSetImplTest extends AbstractLanguageSourceSetTest<dev.nokee.language.cpp.CppSourceSet> {
	@Override
	protected dev.nokee.language.cpp.CppSourceSet newSubject() {
		return new CppSourceSetImpl(project.objects)
	}

	@Override
	protected Class<dev.nokee.language.cpp.CppSourceSet> getPublicType() {
		return dev.nokee.language.cpp.CppSourceSet
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
