package dev.nokee.language.cpp;

import dev.nokee.language.base.testers.LanguageSourceSetTester;
import spock.lang.Subject;

import java.io.File;

import static dev.nokee.language.base.internal.plugins.LanguageBasePlugin.sourceSet;
import static dev.nokee.model.fixtures.ModelRegistryTestUtils.create;
import static dev.nokee.model.fixtures.ModelRegistryTestUtils.registry;

@Subject(CppSourceSet.class)
class CppSourceSetTest extends LanguageSourceSetTester<CppSourceSet> {
	@Override
	public CppSourceSet createSubject() {
		return create(sourceSet("test", CppSourceSet.class));
	}

	@Override
	public CppSourceSet createSubject(File temporaryDirectory) {
		return create(registry(temporaryDirectory), sourceSet("test", CppSourceSet.class));
	}
}
