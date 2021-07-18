package dev.nokee.platform.objectivecpp;

import dev.gradleplugins.grava.testing.util.ProjectTestUtils;
import dev.nokee.language.nativebase.NativeHeaderSet;
import dev.nokee.language.objectivecpp.ObjectiveCppSourceSet;
import dev.nokee.platform.base.testers.ComponentSourcesTester;
import dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin;
import lombok.val;
import spock.lang.Subject;

import java.util.stream.Stream;

import static dev.nokee.model.fixtures.ModelRegistryTestUtils.create;
import static dev.nokee.platform.objectivecpp.internal.plugins.ObjectiveCppLibraryPlugin.objectiveCppLibrary;

@Subject(ObjectiveCppLibrarySources.class)
class ObjectiveCppLibrarySourcesTest implements ComponentSourcesTester<ObjectiveCppLibrarySources> {
	@Override
	public ObjectiveCppLibrarySources createSubject() {
		val project = ProjectTestUtils.rootProject();
		project.getPluginManager().apply(NativeComponentBasePlugin.class);
		val sources = (ObjectiveCppLibrarySources) create(objectiveCppLibrary("main", project)).getSources();
		sources.get(); // force realize
		return sources;
	}

	@Override
	public Stream<SourceSetUnderTest> provideSourceSetUnderTest() {
		return Stream.of(new SourceSetUnderTest("objectiveCpp", ObjectiveCppSourceSet.class), new SourceSetUnderTest("headers", NativeHeaderSet.class), new SourceSetUnderTest("public", NativeHeaderSet.class));
	}
}
