package dev.nokee.language.objectivec.internal

import dev.nokee.language.base.AbstractLanguageSourceSetTest
import dev.nokee.language.objectivec.ObjectiveCSourceSet
import spock.lang.Subject

@Subject(ObjectiveCSourceSetImpl)
class ObjectiveCSourceSetImplTest extends AbstractLanguageSourceSetTest<ObjectiveCSourceSet> {
	@Override
	protected ObjectiveCSourceSet newSubject() {
		return new ObjectiveCSourceSetImpl(newIdentifier(), project.objects)
	}

	@Override
	protected Class<ObjectiveCSourceSet> getPublicType() {
		return ObjectiveCSourceSet
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
