package dev.nokee.language.cpp;

import dev.nokee.language.base.testers.LanguageSourceSetTester;
import spock.lang.Subject;

import java.io.File;

import static dev.nokee.language.base.internal.plugins.LanguageBasePlugin.sourceSet;
import static dev.nokee.model.fixtures.ModelRegistryTestUtils.create;
import static dev.nokee.model.fixtures.ModelRegistryTestUtils.registry;

@Subject(CppHeaderSet.class)
class CppHeaderSetTest extends LanguageSourceSetTester<CppHeaderSet> {
	@Override
	public CppHeaderSet createSubject() {
		return create(sourceSet("test", CppHeaderSet.class));
	}

	@Override
	public CppHeaderSet createSubject(File temporaryDirectory) {
		return create(registry(temporaryDirectory), sourceSet("test", CppHeaderSet.class));
	}
}
