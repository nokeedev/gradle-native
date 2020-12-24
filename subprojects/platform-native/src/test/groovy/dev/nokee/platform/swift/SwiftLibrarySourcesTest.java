package dev.nokee.platform.swift;

import dev.nokee.internal.testing.utils.TestUtils;
import dev.nokee.language.swift.SwiftSourceSet;
import dev.nokee.platform.base.testers.ComponentSourcesTester;
import dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin;
import lombok.val;
import spock.lang.Subject;

import java.util.stream.Stream;

import static dev.nokee.model.fixtures.ModelRegistryTestUtils.create;
import static dev.nokee.platform.swift.internal.plugins.SwiftLibraryPlugin.swiftLibrary;

@Subject(SwiftLibrarySources.class)
public class SwiftLibrarySourcesTest implements ComponentSourcesTester<SwiftLibrarySources> {
	@Override
	public SwiftLibrarySources createSubject() {
		val project = TestUtils.rootProject();
		project.getPluginManager().apply(NativeComponentBasePlugin.class);
		val sources = (SwiftLibrarySources) create(swiftLibrary("main", project)).getSources();
		sources.get(); // force realize
		return sources;
	}

	@Override
	public Stream<SourceSetUnderTest> provideSourceSetUnderTest() {
		return Stream.of(new SourceSetUnderTest("swift", SwiftSourceSet.class));
	}
}
