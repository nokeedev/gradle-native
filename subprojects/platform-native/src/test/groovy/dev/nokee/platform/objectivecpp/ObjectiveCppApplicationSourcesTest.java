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
import static dev.nokee.platform.objectivecpp.internal.plugins.ObjectiveCppApplicationPlugin.objectiveCppApplication;

@Subject(ObjectiveCppApplicationSources.class)
class ObjectiveCppApplicationSourcesTest implements ComponentSourcesTester<ObjectiveCppApplicationSources> {
	@Override
	public ObjectiveCppApplicationSources createSubject() {
		val project = ProjectTestUtils.rootProject();
		project.getPluginManager().apply(NativeComponentBasePlugin.class);
		val sources = (ObjectiveCppApplicationSources) create(objectiveCppApplication("main", project)).getSources();
		sources.get(); // force realize
		return sources;
	}

	@Override
	public Stream<SourceSetUnderTest> provideSourceSetUnderTest() {
		return Stream.of(new SourceSetUnderTest("objectiveCpp", ObjectiveCppSourceSet.class), new SourceSetUnderTest("headers", NativeHeaderSet.class));
	}
}
