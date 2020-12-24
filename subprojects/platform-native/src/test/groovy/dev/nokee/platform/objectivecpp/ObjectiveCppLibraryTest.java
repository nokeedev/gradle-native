package dev.nokee.platform.objectivecpp;

import dev.nokee.internal.testing.utils.TestUtils;
import dev.nokee.language.base.FunctionalSourceSet;
import dev.nokee.language.base.testers.FileSystemWorkspace;
import dev.nokee.language.nativebase.NativeHeaderSet;
import dev.nokee.language.objectivecpp.ObjectiveCppSourceSet;
import dev.nokee.platform.base.testers.SourceAwareComponentTester;
import dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin;
import dev.nokee.platform.objectivecpp.internal.DefaultObjectiveCppLibraryExtension;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import spock.lang.Subject;

import java.io.File;
import java.util.stream.Stream;

import static dev.nokee.model.fixtures.ModelRegistryTestUtils.create;
import static dev.nokee.model.fixtures.ModelRegistryTestUtils.registry;
import static dev.nokee.platform.objectivecpp.internal.plugins.ObjectiveCppLibraryPlugin.objectiveCppLibrary;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;

@Subject(ObjectiveCppLibraryExtension.class)
class ObjectiveCppLibraryTest implements SourceAwareComponentTester<ObjectiveCppLibraryExtension> {
	@Getter @TempDir File testDirectory;

	@Override
	public ObjectiveCppLibraryExtension createSubject(String componentName) {
		val project = TestUtils.createRootProject(getTestDirectory());
		project.getPluginManager().apply(NativeComponentBasePlugin.class);
		val component = create(registry(project.getObjects()), objectiveCppLibrary(componentName, project));
		((FunctionalSourceSet) component.getSources()).get(); // force realize all source set
		return new DefaultObjectiveCppLibraryExtension(component, project.getObjects(), project.getProviders(), project.getLayout());
	}

	@Override
	public Stream<SourcesUnderTest> provideSourceSetUnderTest() {
		return Stream.of(
			new SourcesUnderTest("objectiveCpp", ObjectiveCppSourceSet.class, "objectiveCppSources"),
			new SourcesUnderTest("headers", NativeHeaderSet.class, "privateHeaders"),
			new SourcesUnderTest("public", NativeHeaderSet.class, "publicHeaders"));
	}

	@Test
	public void hasAdditionalConventionOnObjectiveCppSourceSet() throws Throwable {
		val a = new FileSystemWorkspace(getTestDirectory());
		assertThat(createSubject("main").getObjectiveCppSources().getSourceDirectories(),
			hasItem(a.file("src/main/objcpp")));
		assertThat(createSubject("test").getObjectiveCppSources().getSourceDirectories(),
			hasItem(a.file("src/test/objcpp")));
	}
}
