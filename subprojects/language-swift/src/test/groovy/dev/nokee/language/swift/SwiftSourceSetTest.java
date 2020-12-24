package dev.nokee.language.swift;

import dev.nokee.language.base.testers.LanguageSourceSetTester;
import spock.lang.Subject;

import java.io.File;

import static dev.nokee.language.base.internal.plugins.LanguageBasePlugin.sourceSet;
import static dev.nokee.model.fixtures.ModelRegistryTestUtils.create;
import static dev.nokee.model.fixtures.ModelRegistryTestUtils.registry;

@Subject(SwiftSourceSet.class)
class SwiftSourceSetTest extends LanguageSourceSetTester<SwiftSourceSet> {
	@Override
	public SwiftSourceSet createSubject() {
		return create(sourceSet("test", SwiftSourceSet.class));
	}

	@Override
	public SwiftSourceSet createSubject(File temporaryDirectory) {
		return create(registry(temporaryDirectory), sourceSet("test", SwiftSourceSet.class));
	}
}
