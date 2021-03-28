package dev.nokee.platform.ios;

import dev.gradleplugins.grava.testing.util.ProjectTestUtils;
import dev.nokee.language.nativebase.NativeHeaderSet;
import dev.nokee.language.objectivec.ObjectiveCSourceSet;
import dev.nokee.platform.base.testers.ComponentSourcesTester;
import dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin;
import lombok.val;
import spock.lang.Subject;

import java.util.stream.Stream;

import static dev.nokee.model.fixtures.ModelRegistryTestUtils.create;
import static dev.nokee.platform.ios.internal.plugins.ObjectiveCIosApplicationPlugin.objectiveCIosApplication;

@Subject(ObjectiveCIosApplicationSources.class)
class ObjectiveCIosApplicationSourcesTest implements ComponentSourcesTester<ObjectiveCIosApplicationSources> {
	@Override
	public ObjectiveCIosApplicationSources createSubject() {
		val project = ProjectTestUtils.rootProject();
		project.getPluginManager().apply(NativeComponentBasePlugin.class);
		val sources = (ObjectiveCIosApplicationSources) create(objectiveCIosApplication("main", project)).getSources();
		sources.get(); // force realize
		return sources;
	}

	@Override
	public Stream<SourceSetUnderTest> provideSourceSetUnderTest() {
		return Stream.of(new SourceSetUnderTest("objectiveC", ObjectiveCSourceSet.class), new SourceSetUnderTest("headers", NativeHeaderSet.class), new SourceSetUnderTest("resources", IosResourceSet.class));
	}
}
