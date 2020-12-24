package dev.nokee.platform.cpp;

import dev.nokee.internal.testing.utils.TestUtils;
import dev.nokee.language.cpp.CppSourceSet;
import dev.nokee.language.nativebase.NativeHeaderSet;
import dev.nokee.platform.base.testers.ComponentSourcesTester;
import dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin;
import lombok.val;
import spock.lang.Subject;

import java.util.stream.Stream;

import static dev.nokee.model.fixtures.ModelRegistryTestUtils.create;
import static dev.nokee.platform.cpp.internal.plugins.CppLibraryPlugin.cppLibrary;

@Subject(CppLibrarySources.class)
class CppLibrarySourcesTest implements ComponentSourcesTester<CppLibrarySources> {

	@Override
	public CppLibrarySources createSubject() {
		val project = TestUtils.rootProject();
		project.getPluginManager().apply(NativeComponentBasePlugin.class);
		val sources = (CppLibrarySources) create(cppLibrary("main", project)).getSources();
		sources.get(); // force realize
		return sources;
	}

	@Override
	public Stream<SourceSetUnderTest> provideSourceSetUnderTest() {
		return Stream.of(new SourceSetUnderTest("cpp", CppSourceSet.class), new SourceSetUnderTest("headers", NativeHeaderSet.class), new SourceSetUnderTest("public", NativeHeaderSet.class));
	}
}
