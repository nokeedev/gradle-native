package dev.nokee.language.c;

import dev.nokee.language.base.testers.LanguageSourceSetTester;
import spock.lang.Subject;

import java.io.File;

import static dev.nokee.language.base.internal.plugins.LanguageBasePlugin.sourceSet;
import static dev.nokee.model.fixtures.ModelRegistryTestUtils.create;
import static dev.nokee.model.fixtures.ModelRegistryTestUtils.registry;

@Subject(CSourceSet.class)
class CSourceSetTest extends LanguageSourceSetTester<CSourceSet> {
	@Override
	public CSourceSet createSubject() {
		return create(sourceSet("test", CSourceSet.class));
	}

	@Override
	public CSourceSet createSubject(File temporaryDirectory) {
		return create(registry(temporaryDirectory), sourceSet("test", CSourceSet.class));
	}
}
