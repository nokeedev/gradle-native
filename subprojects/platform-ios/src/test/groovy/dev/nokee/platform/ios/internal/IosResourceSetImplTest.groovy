package dev.nokee.platform.ios.internal

import dev.nokee.language.base.AbstractLanguageSourceSetTest
import dev.nokee.platform.ios.IosResourceSet
import dev.nokee.platform.ios.internal.IosResourceSetImpl
import spock.lang.Subject

@Subject(IosResourceSetImpl)
class IosResourceSetImplTest extends AbstractLanguageSourceSetTest<IosResourceSet> {
	@Override
	protected IosResourceSetImpl newSubject() {
		return new IosResourceSetImpl(newIdentifier(), project.objects)
	}

	@Override
	protected Class<IosResourceSet> getPublicType() {
		return IosResourceSet
	}

	@Override
	protected Set<String> getDefaultFilterIncludes() {
		return []
	}

	@Override
	protected String fileName(String fileName) {
		return fileName
	}
}
