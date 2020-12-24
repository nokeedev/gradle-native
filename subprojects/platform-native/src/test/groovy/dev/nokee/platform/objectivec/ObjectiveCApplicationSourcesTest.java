package dev.nokee.platform.objectivec;

import dev.nokee.internal.testing.utils.TestUtils;
import dev.nokee.language.nativebase.NativeHeaderSet;
import dev.nokee.language.objectivec.ObjectiveCSourceSet;
import dev.nokee.platform.base.testers.ComponentSourcesTester;
import dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin;
import lombok.val;
import spock.lang.Subject;

import java.util.stream.Stream;

import static dev.nokee.model.fixtures.ModelRegistryTestUtils.create;
import static dev.nokee.platform.objectivec.internal.plugins.ObjectiveCApplicationPlugin.objectiveCApplication;

@Subject(ObjectiveCApplicationSources.class)
class ObjectiveCApplicationSourcesTest implements ComponentSourcesTester<ObjectiveCApplicationSources> {
	@Override
	public ObjectiveCApplicationSources createSubject() {
		val project = TestUtils.rootProject();
		project.getPluginManager().apply(NativeComponentBasePlugin.class);
		val sources = (ObjectiveCApplicationSources) create(objectiveCApplication("main", project)).getSources();
		sources.get(); // force realize
		return sources;
	}

	@Override
	public Stream<SourceSetUnderTest> provideSourceSetUnderTest() {
		return Stream.of(new SourceSetUnderTest("objectiveC", ObjectiveCSourceSet.class), new SourceSetUnderTest("headers", NativeHeaderSet.class));
	}
}
