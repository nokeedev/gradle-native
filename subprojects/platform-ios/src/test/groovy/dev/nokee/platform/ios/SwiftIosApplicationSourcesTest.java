package dev.nokee.platform.ios;

import dev.nokee.internal.testing.utils.TestUtils;
import dev.nokee.language.swift.SwiftSourceSet;
import dev.nokee.platform.base.testers.ComponentSourcesTester;
import dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin;
import lombok.val;
import spock.lang.Subject;

import java.util.stream.Stream;

import static dev.nokee.model.fixtures.ModelRegistryTestUtils.create;
import static dev.nokee.platform.ios.internal.plugins.SwiftIosApplicationPlugin.swiftIosApplication;

@Subject(SwiftIosApplicationSources.class)
class SwiftIosApplicationSourcesTest implements ComponentSourcesTester<SwiftIosApplicationSources> {
	@Override
	public SwiftIosApplicationSources createSubject() {
		val project = TestUtils.rootProject();
		project.getPluginManager().apply(NativeComponentBasePlugin.class);
		val sources = (SwiftIosApplicationSources) create(swiftIosApplication("main", project)).getSources();
		sources.get(); // force realize
		return sources;
	}

	@Override
	public Stream<SourceSetUnderTest> provideSourceSetUnderTest() {
		return Stream.of(new SourceSetUnderTest("swift", SwiftSourceSet.class), new SourceSetUnderTest("resources", IosResourceSet.class));
	}
}
