package dev.nokee.language.base;

import dev.nokee.language.base.internal.BaseLanguageSourceSetProjection;
import dev.nokee.language.base.testers.LanguageSourceSetTester;
import spock.lang.Subject;

import java.io.File;

import static dev.nokee.language.base.internal.plugins.LanguageBasePlugin.sourceSet;
import static dev.nokee.model.fixtures.ModelRegistryTestUtils.create;
import static dev.nokee.model.fixtures.ModelRegistryTestUtils.registry;

@Subject({LanguageSourceSet.class, BaseLanguageSourceSetProjection.class})
class LanguageSourceSetTest extends LanguageSourceSetTester<LanguageSourceSet> {
	@Override
	public LanguageSourceSet createSubject() {
		return create(sourceSet("test", LanguageSourceSet.class));
	}

	@Override
	public LanguageSourceSet createSubject(File baseDirectory) {
		return create(registry(baseDirectory), sourceSet("test", LanguageSourceSet.class));
	}
}
