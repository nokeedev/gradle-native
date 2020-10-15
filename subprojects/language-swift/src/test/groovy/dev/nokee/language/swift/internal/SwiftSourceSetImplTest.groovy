package dev.nokee.language.swift.internal

import dev.nokee.language.base.AbstractLanguageSourceSetTest
import dev.nokee.language.swift.SwiftSourceSet
import spock.lang.Subject

@Subject(SwiftSourceSetImpl)
class SwiftSourceSetImplTest extends AbstractLanguageSourceSetTest<SwiftSourceSet> {
	@Override
	protected SwiftSourceSet newSubject() {
		return new SwiftSourceSetImpl(newIdentifier(), project.objects)
	}

	@Override
	protected Class<SwiftSourceSet> getPublicType() {
		return SwiftSourceSet
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
