package dev.nokee.platform.cpp;

import dev.nokee.internal.testing.utils.TestUtils;
import dev.nokee.language.base.FunctionalSourceSet;
import dev.nokee.language.cpp.CppSourceSet;
import dev.nokee.language.nativebase.NativeHeaderSet;
import dev.nokee.platform.base.testers.SourceAwareComponentTester;
import dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.io.TempDir;
import spock.lang.Subject;

import java.io.File;
import java.util.stream.Stream;

import static dev.nokee.model.fixtures.ModelRegistryTestUtils.create;
import static dev.nokee.model.fixtures.ModelRegistryTestUtils.registry;
import static dev.nokee.platform.cpp.internal.plugins.CppLibraryPlugin.cppLibrary;

@Subject(CppLibrary.class)
class CppLibraryTest implements SourceAwareComponentTester<CppLibrary> {
	@Getter @TempDir File testDirectory;

	@Override
	public CppLibrary createSubject(String componentName) {
		val project = TestUtils.createRootProject(getTestDirectory());
		project.getPluginManager().apply(NativeComponentBasePlugin.class);
		val component = create(registry(project.getObjects()), cppLibrary(componentName, project));
		((FunctionalSourceSet) component.getSources()).get(); // force realize all source set
		return component;
	}

	@Override
	public Stream<SourcesUnderTest> provideSourceSetUnderTest() {
		return Stream.of(
			new SourcesUnderTest("cpp", CppSourceSet.class, "cppSources"),
			new SourcesUnderTest("headers", NativeHeaderSet.class, "privateHeaders"),
			new SourcesUnderTest("public", NativeHeaderSet.class, "publicHeaders"));
	}
}
