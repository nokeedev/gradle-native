package dev.nokee.platform.c;

import dev.gradleplugins.grava.testing.util.ProjectTestUtils;
import dev.nokee.language.c.CSourceSet;
import dev.nokee.language.nativebase.NativeHeaderSet;
import dev.nokee.platform.base.testers.ComponentSourcesTester;
import dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin;
import lombok.val;
import spock.lang.Subject;

import java.util.stream.Stream;

import static dev.nokee.model.fixtures.ModelRegistryTestUtils.create;
import static dev.nokee.platform.c.internal.plugins.CLibraryPlugin.cLibrary;

@Subject(CLibrarySources.class)
class CLibrarySourcesTest implements ComponentSourcesTester<CLibrarySources> {
	@Override
	public CLibrarySources createSubject() {
		val project = ProjectTestUtils.rootProject();
		project.getPluginManager().apply(NativeComponentBasePlugin.class);
		val sources = (CLibrarySources) create(cLibrary("main", project)).getSources();
		sources.get(); // force realize
		return sources;
	}

	@Override
	public Stream<SourceSetUnderTest> provideSourceSetUnderTest() {
		return Stream.of(new SourceSetUnderTest("c", CSourceSet.class),
			new SourceSetUnderTest("headers", NativeHeaderSet.class),
			new SourceSetUnderTest("public", NativeHeaderSet.class));
	}
}
