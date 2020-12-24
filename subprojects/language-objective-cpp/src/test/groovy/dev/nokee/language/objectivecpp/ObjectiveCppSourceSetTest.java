package dev.nokee.language.objectivecpp;

import dev.nokee.language.base.testers.LanguageSourceSetTester;
import spock.lang.Subject;

import java.io.File;

import static dev.nokee.language.base.internal.plugins.LanguageBasePlugin.sourceSet;
import static dev.nokee.model.fixtures.ModelRegistryTestUtils.create;
import static dev.nokee.model.fixtures.ModelRegistryTestUtils.registry;

@Subject(ObjectiveCppSourceSet.class)
class ObjectiveCppSourceSetTest extends LanguageSourceSetTester<ObjectiveCppSourceSet> {
	@Override
	public ObjectiveCppSourceSet createSubject() {
		return create(sourceSet("test", ObjectiveCppSourceSet.class));
	}

	@Override
	public ObjectiveCppSourceSet createSubject(File temporaryDirectory) {
		return create(registry(temporaryDirectory), sourceSet("test", ObjectiveCppSourceSet.class));
	}
}
