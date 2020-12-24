package dev.nokee.platform.ios;

import dev.nokee.language.base.testers.LanguageSourceSetTester;
import spock.lang.Subject;

import java.io.File;

import static dev.nokee.language.base.internal.plugins.LanguageBasePlugin.sourceSet;
import static dev.nokee.model.fixtures.ModelRegistryTestUtils.create;
import static dev.nokee.model.fixtures.ModelRegistryTestUtils.registry;

@Subject(IosResourceSet.class)
class IosResourceSetTest extends LanguageSourceSetTester<IosResourceSet> {
	@Override
	public IosResourceSet createSubject() {
		return create(sourceSet("test", IosResourceSet.class));
	}

	@Override
	public IosResourceSet createSubject(File temporaryDirectory) {
		return create(registry(temporaryDirectory), sourceSet("test", IosResourceSet.class));
	}
}
