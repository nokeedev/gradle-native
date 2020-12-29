package dev.nokee.platform.ios;

import dev.nokee.fixtures.NativeComponentMatchers;
import dev.nokee.internal.testing.utils.TestUtils;
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
import static dev.nokee.platform.ios.internal.plugins.SwiftIosApplicationPlugin.swiftIosApplication;

@Subject(SwiftIosApplication.class)
class SwiftIosApplicationTest implements SourceAwareComponentTester<SwiftIosApplication>, BaseNameAwareComponentTester {
	@Getter @TempDir File testDirectory;

	@Override
	public SwiftIosApplication createSubject(String componentName) {
		val project = TestUtils.createRootProject(getTestDirectory());
		project.getPluginManager().apply(NativeComponentBasePlugin.class);
		val component = create(registry(project.getObjects()), swiftIosApplication(componentName, project));
		((FunctionalSourceSet) component.getSources()).get(); // force realize all source set
		return component;
	}

	@Override
	public Matcher<Component> hasArtifactBaseNameOf(String name) {
		return NativeComponentMatchers.hasArtifactBaseNameOf(name);
	}

	@Override
	public Stream<SourcesUnderTest> provideSourceSetUnderTest() {
		return Stream.of(new SourcesUnderTest("swift", SwiftSourceSet.class, "swiftSources"), new SourcesUnderTest("resources", IosResourceSet.class, "resources"));
	}
}
