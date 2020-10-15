package dev.nokee.language.objectivec.internal

import dev.nokee.language.base.AbstractLanguageSourceSetTest
import spock.lang.Subject

@Subject(ObjectiveCSourceSetImpl)
class ObjectiveCSourceSetImplTest extends AbstractLanguageSourceSetTest<dev.nokee.language.objectivec.ObjectiveCSourceSet> {
	@Override
	protected dev.nokee.language.objectivec.ObjectiveCSourceSet newSubject() {
		return new ObjectiveCSourceSetImpl(project.objects)
	}

	@Override
	protected Class<dev.nokee.language.objectivec.ObjectiveCSourceSet> getPublicType() {
		return dev.nokee.language.objectivec.ObjectiveCSourceSet
	}

	@Override
	protected Set<String> getDefaultFilterIncludes() {
		return ['**/*.m']
	}

	@Override
	protected String fileName(String fileName) {
		return fileName + '.m'
	}
}
