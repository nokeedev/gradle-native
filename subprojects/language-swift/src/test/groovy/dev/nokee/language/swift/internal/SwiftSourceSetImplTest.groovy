package dev.nokee.language.swift.internal

import dev.nokee.language.base.AbstractLanguageSourceSetTest
import spock.lang.Subject

@Subject(SwiftSourceSetImpl)
class SwiftSourceSetImplTest extends AbstractLanguageSourceSetTest<dev.nokee.language.swift.SwiftSourceSet> {
	@Override
	protected dev.nokee.language.swift.SwiftSourceSet newSubject() {
		return new SwiftSourceSetImpl(project.objects)
	}

	@Override
	protected Class<dev.nokee.language.swift.SwiftSourceSet> getPublicType() {
		return dev.nokee.language.swift.SwiftSourceSet
	}

	@Override
	protected Set<String> getDefaultFilterIncludes() {
		return ['**/*.swift']
	}

	@Override
	protected String fileName(String fileName) {
		return fileName + '.swift'
	}
}
