package dev.nokee.platform.swift;

import dev.gradleplugins.grava.testing.util.ProjectTestUtils;
import dev.nokee.fixtures.NativeComponentMatchers;
import dev.nokee.language.base.FunctionalSourceSet;
import dev.nokee.language.swift.SwiftSourceSet;
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
import static dev.nokee.platform.swift.internal.plugins.SwiftLibraryPlugin.swiftLibrary;

@Subject(SwiftLibrary.class)
public class SwiftLibraryTest implements SourceAwareComponentTester<SwiftLibrary>, BaseNameAwareComponentTester {
	@Getter @TempDir File testDirectory;

	@Override
	public SwiftLibrary createSubject(String componentName) {
		val project = ProjectTestUtils.createRootProject(testDirectory);
		project.getPluginManager().apply(NativeComponentBasePlugin.class);
		val component = create(registry(project.getObjects()), swiftLibrary(componentName, project));
		((FunctionalSourceSet) component.getSources()).get(); // force realize
		return component;
	}

	@Override
	public Matcher<Component> hasArtifactBaseNameOf(String name) {
		return NativeComponentMatchers.hasArtifactBaseNameOf(name);
	}

	@Override
	public Stream<SourcesUnderTest> provideSourceSetUnderTest() {
		return Stream.of(new SourcesUnderTest("swift", SwiftSourceSet.class, "swiftSources"));
	}
}
