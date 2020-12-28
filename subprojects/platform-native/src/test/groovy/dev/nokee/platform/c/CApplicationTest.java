package dev.nokee.platform.c;

import dev.nokee.internal.testing.utils.TestUtils;
import dev.nokee.language.base.FunctionalSourceSet;
import dev.nokee.language.c.CSourceSet;
import dev.nokee.language.nativebase.NativeHeaderSet;
import dev.nokee.platform.base.testers.SourceAwareComponentTester;
import dev.nokee.platform.c.internal.DefaultCApplicationExtension;
import dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.io.TempDir;
import spock.lang.Subject;

import java.io.File;
import java.util.stream.Stream;

import static dev.nokee.model.fixtures.ModelRegistryTestUtils.create;
import static dev.nokee.model.fixtures.ModelRegistryTestUtils.registry;
import static dev.nokee.platform.c.internal.plugins.CApplicationPlugin.cApplication;

@Subject(CApplicationExtension.class)
class CApplicationTest implements SourceAwareComponentTester<CApplicationExtension> {
	@Getter @TempDir File testDirectory;

	@Override
	public CApplicationExtension createSubject(String componentName) {
		val project = TestUtils.createRootProject(getTestDirectory());
		project.getPluginManager().apply(NativeComponentBasePlugin.class);
		val component = create(registry(project.getObjects()), cApplication(componentName, project));
		((FunctionalSourceSet) component.getSources()).get(); // force realize all source set
		return new DefaultCApplicationExtension(component, project.getObjects(), project.getProviders(), project.getLayout());
	}

	@Override
	public Stream<SourcesUnderTest> provideSourceSetUnderTest() {
		return Stream.of(
			new SourcesUnderTest("c", CSourceSet.class, "cSources"),
			new SourcesUnderTest("headers", NativeHeaderSet.class, "privateHeaders"));
	}
}
