package dev.nokee.language.objectivecpp.internal

import dev.nokee.language.base.AbstractLanguageSourceSetTest
import spock.lang.Subject

@Subject(ObjectiveCppSourceSetImpl)
class ObjectiveCppSourceSetImplTest extends AbstractLanguageSourceSetTest<dev.nokee.language.objectivecpp.ObjectiveCppSourceSet> {
	@Override
	protected dev.nokee.language.objectivecpp.ObjectiveCppSourceSet newSubject() {
		return new ObjectiveCppSourceSetImpl(project.objects)
	}

	@Override
	protected Class<dev.nokee.language.objectivecpp.ObjectiveCppSourceSet> getPublicType() {
		return dev.nokee.language.objectivec.ObjectiveCSourceSet
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
