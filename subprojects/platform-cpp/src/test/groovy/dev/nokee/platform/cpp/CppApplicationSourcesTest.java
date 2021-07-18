package dev.nokee.platform.cpp;

import dev.gradleplugins.grava.testing.util.ProjectTestUtils;
import dev.nokee.language.cpp.CppSourceSet;
import dev.nokee.language.nativebase.NativeHeaderSet;
import dev.nokee.platform.base.testers.ComponentSourcesTester;
import dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin;
import lombok.val;
import spock.lang.Subject;

import java.util.stream.Stream;

import static dev.nokee.model.fixtures.ModelRegistryTestUtils.create;
import static dev.nokee.platform.cpp.internal.plugins.CppApplicationPlugin.cppApplication;

@Subject(CppApplicationSources.class)
class CppApplicationSourcesTest implements ComponentSourcesTester<CppApplicationSources> {

	@Override
	public CppApplicationSources createSubject() {
		val project = ProjectTestUtils.rootProject();
		project.getPluginManager().apply(NativeComponentBasePlugin.class);
		val sources = (CppApplicationSources) create(cppApplication("main", project)).getSources();
		sources.get(); // force realize
		return sources;
	}

	@Override
	public Stream<SourceSetUnderTest> provideSourceSetUnderTest() {
		return Stream.of(new SourceSetUnderTest("cpp", CppSourceSet.class), new SourceSetUnderTest("headers", NativeHeaderSet.class));
	}
}
