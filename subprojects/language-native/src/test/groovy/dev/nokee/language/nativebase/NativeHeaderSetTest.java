package dev.nokee.language.nativebase;

import dev.nokee.language.base.testers.LanguageSourceSetTester;
import spock.lang.Subject;

import java.io.File;

import static dev.nokee.language.base.internal.plugins.LanguageBasePlugin.sourceSet;
import static dev.nokee.model.fixtures.ModelRegistryTestUtils.create;
import static dev.nokee.model.fixtures.ModelRegistryTestUtils.registry;

@Subject(NativeHeaderSet.class)
public class NativeHeaderSetTest extends LanguageSourceSetTester<NativeHeaderSet> {
	@Override
	public NativeHeaderSet createSubject() {
		return create(sourceSet("test", NativeHeaderSet.class));
	}

	@Override
	public NativeHeaderSet createSubject(File temporaryDirectory) {
		return create(registry(temporaryDirectory), sourceSet("test", NativeHeaderSet.class));
	}
}
