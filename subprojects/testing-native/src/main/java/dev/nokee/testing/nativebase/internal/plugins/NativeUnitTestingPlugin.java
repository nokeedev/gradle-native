package dev.nokee.testing.nativebase.internal.plugins;

import dev.nokee.language.c.internal.CSourceSet;
import dev.nokee.language.cpp.internal.CppSourceSet;
import dev.nokee.language.objectivec.internal.ObjectiveCSourceSet;
import dev.nokee.language.objectivecpp.internal.ObjectiveCppSourceSet;
import dev.nokee.language.swift.internal.SwiftSourceSet;
import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.testing.base.TestSuiteContainer;
import dev.nokee.testing.base.internal.DefaultTestSuiteContainer;
import dev.nokee.testing.base.internal.plugins.TestingBasePlugin;
import dev.nokee.testing.nativebase.NativeTestSuite;
import dev.nokee.testing.nativebase.internal.DefaultNativeTestSuiteComponent;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.TaskContainer;

import javax.inject.Inject;

public abstract class NativeUnitTestingPlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply("lifecycle-base");
		project.getPluginManager().apply(TestingBasePlugin.class);

		val extension = (DefaultTestSuiteContainer)project.getExtensions().getByType(TestSuiteContainer.class);
		extension.registerFactory(NativeTestSuite.class, DefaultNativeTestSuiteComponent.class, this::createNativeTestSuite);

		extension.whenElementKnown(DefaultNativeTestSuiteComponent.class, knownTestSuite -> {
			knownTestSuite.configure(testSuite -> {
				// TODO: Move these generic source set creation to the respective language plugin
				if (project.getPluginManager().hasPlugin("dev.nokee.c-language")) {
					testSuite.getSourceCollection().add(getObjects().newInstance(CSourceSet.class, "c").from(testSuite.getNames().getSourceSetPath("c")));
				}
				if (project.getPluginManager().hasPlugin("dev.nokee.cpp-language")) {
					testSuite.getSourceCollection().add(getObjects().newInstance(CppSourceSet.class, "cpp").from(testSuite.getNames().getSourceSetPath("cpp")));
				}
				if (project.getPluginManager().hasPlugin("dev.nokee.objective-c-language")) {
					testSuite.getSourceCollection().add(getObjects().newInstance(ObjectiveCSourceSet.class, "objc").from(testSuite.getNames().getSourceSetPath("objc")));
				}
				if (project.getPluginManager().hasPlugin("dev.nokee.objective-cpp-language")) {
					testSuite.getSourceCollection().add(getObjects().newInstance(ObjectiveCppSourceSet.class, "objcpp").from(testSuite.getNames().getSourceSetPath("objcpp")));
				}
				if (project.getPluginManager().hasPlugin("dev.nokee.swift-language")) {
					testSuite.getSourceCollection().add(getObjects().newInstance(SwiftSourceSet.class, "swift").from(testSuite.getNames().getSourceSetPath("swift")));
				}
			});
		});

		project.afterEvaluate(proj -> {
			// TODO: We delay as late as possible to "fake" a finalize action.
			extension.configureEach(DefaultNativeTestSuiteComponent.class, it -> it.finalizeExtension(proj));
			extension.forceRealize();
		});
	}

	@Inject
	protected abstract ObjectFactory getObjects();

	@Inject
	protected abstract TaskContainer getTasks();

	private NativeTestSuite createNativeTestSuite(String name) {
		return getObjects().newInstance(DefaultNativeTestSuiteComponent.class, NamingScheme.asComponent(name, name).withComponentDisplayName("Test Suite"));
	}
}
