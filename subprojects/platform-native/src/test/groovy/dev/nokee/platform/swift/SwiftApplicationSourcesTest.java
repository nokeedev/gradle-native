package dev.nokee.platform.swift;

import dev.nokee.internal.testing.utils.TestUtils;
import dev.nokee.language.swift.SwiftSourceSet;
import dev.nokee.platform.base.testers.ComponentSourcesTester;
import dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin;
import lombok.val;
import spock.lang.Subject;

import java.util.stream.Stream;

import static dev.nokee.model.fixtures.ModelRegistryTestUtils.create;
import static dev.nokee.platform.swift.internal.plugins.SwiftApplicationPlugin.swiftApplication;

@Subject(SwiftApplicationSources.class)
class SwiftApplicationSourcesTest implements ComponentSourcesTester<SwiftApplicationSources> {
	@Override
	public SwiftApplicationSources createSubject() {
		val project = TestUtils.rootProject();
		project.getPluginManager().apply(NativeComponentBasePlugin.class);
		val sources = (SwiftApplicationSources) create(swiftApplication("main", project)).getSources();
		sources.get(); // force realize
		return sources;
	}

	@Override
	public Stream<SourceSetUnderTest> provideSourceSetUnderTest() {
		return Stream.of(new SourceSetUnderTest("swift", SwiftSourceSet.class));
	}
}
