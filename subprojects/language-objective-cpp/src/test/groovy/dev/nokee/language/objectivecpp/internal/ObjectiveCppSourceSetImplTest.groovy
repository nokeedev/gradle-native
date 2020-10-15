package dev.nokee.language.objectivecpp.internal

import dev.nokee.language.base.AbstractLanguageSourceSetTest
import dev.nokee.language.objectivecpp.ObjectiveCppSourceSet
import spock.lang.Subject

@Subject(ObjectiveCppSourceSetImpl)
class ObjectiveCppSourceSetImplTest extends AbstractLanguageSourceSetTest<ObjectiveCppSourceSet> {
	@Override
	protected ObjectiveCppSourceSet newSubject() {
		return new ObjectiveCppSourceSetImpl(newIdentifier(), project.objects)
	}

	@Override
	protected Class<ObjectiveCppSourceSet> getPublicType() {
		return ObjectiveCppSourceSet
	}

	@Override
	protected Set<String> getDefaultFilterIncludes() {
		return ['**/*.mm']
	}

	@Override
	protected String fileName(String fileName) {
		return fileName + '.mm'
	}
}
