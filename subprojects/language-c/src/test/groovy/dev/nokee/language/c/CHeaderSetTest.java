package dev.nokee.language.c;

import dev.nokee.language.base.testers.LanguageSourceSetTester;
import spock.lang.Subject;

import java.io.File;

import static dev.nokee.language.base.internal.plugins.LanguageBasePlugin.sourceSet;
import static dev.nokee.model.fixtures.ModelRegistryTestUtils.create;
import static dev.nokee.model.fixtures.ModelRegistryTestUtils.registry;

@Subject(CHeaderSet.class)
class CHeaderSetTest extends LanguageSourceSetTester<CHeaderSet> {
	@Override
	public CHeaderSet createSubject() {
		return create(sourceSet("test", CHeaderSet.class));
	}

	@Override
	public CHeaderSet createSubject(File temporaryDirectory) {
		return create(registry(temporaryDirectory), sourceSet("test", CHeaderSet.class));
	}
}
