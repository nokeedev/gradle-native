package dev.nokee.platform.c;

import dev.nokee.internal.testing.utils.TestUtils;
import dev.nokee.language.c.CSourceSet;
import dev.nokee.language.nativebase.NativeHeaderSet;
import dev.nokee.platform.base.testers.ComponentSourcesTester;
import dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin;
import lombok.val;
import spock.lang.Subject;

import java.util.stream.Stream;

import static dev.nokee.model.fixtures.ModelRegistryTestUtils.create;
import static dev.nokee.platform.c.internal.plugins.CApplicationPlugin.cApplication;

@Subject(CApplicationSources.class)
class CApplicationSourcesTest implements ComponentSourcesTester<CApplicationSources> {
	@Override
	public CApplicationSources createSubject() {
		val project = TestUtils.rootProject();
		project.getPluginManager().apply(NativeComponentBasePlugin.class);
		val sources = (CApplicationSources) create(cApplication("main", project)).getSources();
		sources.get(); // force realize
		return sources;
	}

	@Override
	public Stream<SourceSetUnderTest> provideSourceSetUnderTest() {
		return Stream.of(new SourceSetUnderTest("c", CSourceSet.class), new SourceSetUnderTest("headers", NativeHeaderSet.class));
	}
}
