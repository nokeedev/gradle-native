package dev.nokee.platform.cpp;

import dev.nokee.fixtures.NativeComponentMatchers;
import dev.nokee.internal.testing.utils.TestUtils;
import dev.nokee.language.base.FunctionalSourceSet;
import dev.nokee.language.cpp.CppSourceSet;
import dev.nokee.language.nativebase.NativeHeaderSet;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.testers.BaseNameAwareComponentTester;
import dev.nokee.platform.base.testers.SourceAwareComponentTester;
import dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin;
import lombok.Getter;
import lombok.val;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.io.TempDir;
import spock.lang.Subject;

import java.io.File;
import java.util.stream.Stream;

import static dev.nokee.model.fixtures.ModelRegistryTestUtils.create;
import static dev.nokee.model.fixtures.ModelRegistryTestUtils.registry;
import static dev.nokee.platform.cpp.internal.plugins.CppApplicationPlugin.cppApplication;

@Subject(CppApplication.class)
class CppApplicationTest implements SourceAwareComponentTester<CppApplication>, BaseNameAwareComponentTester {
	@Getter @TempDir File testDirectory;

	@Override
	public CppApplication createSubject(String componentName) {
		val project = TestUtils.createRootProject(getTestDirectory());
		project.getPluginManager().apply(NativeComponentBasePlugin.class);
		val component = create(registry(project.getObjects()), cppApplication(componentName, project));
		((FunctionalSourceSet) component.getSources()).get(); // force realize all source set
		return component;
	}

	@Override
	public Matcher<Component> hasArtifactBaseNameOf(String name) {
		return NativeComponentMatchers.hasArtifactBaseNameOf(name);
	}

	@Override
	public Stream<SourcesUnderTest> provideSourceSetUnderTest() {
		return Stream.of(
			new SourcesUnderTest("cpp", CppSourceSet.class, "cppSources"),
			new SourcesUnderTest("headers", NativeHeaderSet.class, "privateHeaders"));
	}
}
