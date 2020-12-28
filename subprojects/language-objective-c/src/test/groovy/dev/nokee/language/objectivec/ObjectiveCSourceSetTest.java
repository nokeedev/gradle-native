package dev.nokee.language.objectivec;

import dev.nokee.language.base.testers.LanguageSourceSetTester;
import spock.lang.Subject;

import java.io.File;

import static dev.nokee.language.base.internal.plugins.LanguageBasePlugin.sourceSet;
import static dev.nokee.model.fixtures.ModelRegistryTestUtils.create;
import static dev.nokee.model.fixtures.ModelRegistryTestUtils.registry;

@Subject(ObjectiveCSourceSet.class)
class ObjectiveCSourceSetTest extends LanguageSourceSetTester<ObjectiveCSourceSet> {
	@Override
	public ObjectiveCSourceSet createSubject() {
		return create(sourceSet("test", ObjectiveCSourceSet.class));
	}

	@Override
	public ObjectiveCSourceSet createSubject(File temporaryDirectory) {
		return create(registry(temporaryDirectory), sourceSet("test", ObjectiveCSourceSet.class));
	}
}
