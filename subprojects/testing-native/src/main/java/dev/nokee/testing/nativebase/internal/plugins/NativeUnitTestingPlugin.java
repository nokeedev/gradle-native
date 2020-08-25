package dev.nokee.testing.nativebase.internal.plugins;

import dev.nokee.language.base.internal.DaggerLanguageSourceSetInstantiatorComponent;
import dev.nokee.language.c.CSourceSet;
import dev.nokee.language.cpp.CppSourceSet;
import dev.nokee.language.objectivec.ObjectiveCSourceSet;
import dev.nokee.language.objectivecpp.ObjectiveCppSourceSet;
import dev.nokee.language.swift.SwiftSourceSet;
import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.testing.base.TestSuiteContainer;
import dev.nokee.testing.base.internal.DefaultTestSuiteContainer;
import dev.nokee.testing.base.internal.plugins.TestingBasePlugin;
import dev.nokee.testing.nativebase.NativeTestSuite;
import dev.nokee.testing.nativebase.internal.DefaultNativeTestSuiteComponent;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.TaskContainer;

import javax.inject.Inject;

import static dev.nokee.model.DomainObjectIdentifier.named;

public class NativeUnitTestingPlugin implements Plugin<Project> {
	@Getter(AccessLevel.PROTECTED) private final ObjectFactory objects;
	@Getter(AccessLevel.PROTECTED) private final TaskContainer tasks;

	@Inject
	public NativeUnitTestingPlugin(ObjectFactory objects, TaskContainer tasks) {
		this.objects = objects;
		this.tasks = tasks;
	}

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply("lifecycle-base");
		project.getPluginManager().apply(TestingBasePlugin.class);

		val extension = (DefaultTestSuiteContainer)project.getExtensions().getByType(TestSuiteContainer.class);
		extension.registerFactory(NativeTestSuite.class, DefaultNativeTestSuiteComponent.class, this::createNativeTestSuite);

		extension.whenElementKnown(DefaultNativeTestSuiteComponent.class, knownTestSuite -> {
			knownTestSuite.configure(testSuite -> {
				val sourceSetIntantiator = DaggerLanguageSourceSetInstantiatorComponent.factory().create(project).get();
				// TODO: Move these generic source set creation to the respective language plugin
				if (project.getPluginManager().hasPlugin("dev.nokee.c-language")) {
					testSuite.getSourceCollection().add(sourceSetIntantiator.create(named("c"), CSourceSet.class).from(testSuite.getNames().getSourceSetPath("c")));
				}
				if (project.getPluginManager().hasPlugin("dev.nokee.cpp-language")) {
					testSuite.getSourceCollection().add(sourceSetIntantiator.create(named("cpp"), CppSourceSet.class).from(testSuite.getNames().getSourceSetPath("cpp")));
				}
				if (project.getPluginManager().hasPlugin("dev.nokee.objective-c-language")) {
					testSuite.getSourceCollection().add(sourceSetIntantiator.create(named("objc"), ObjectiveCSourceSet.class).from(testSuite.getNames().getSourceSetPath("objc")));
				}
				if (project.getPluginManager().hasPlugin("dev.nokee.objective-cpp-language")) {
					testSuite.getSourceCollection().add(sourceSetIntantiator.create(named("objcpp"), ObjectiveCppSourceSet.class).from(testSuite.getNames().getSourceSetPath("objcpp")));
				}
				if (project.getPluginManager().hasPlugin("dev.nokee.swift-language")) {
					testSuite.getSourceCollection().add(sourceSetIntantiator.create(named("swift"), SwiftSourceSet.class).from(testSuite.getNames().getSourceSetPath("swift")));
				}
			});
		});

		project.afterEvaluate(proj -> {
			// TODO: We delay as late as possible to "fake" a finalize action.
			extension.configureEach(DefaultNativeTestSuiteComponent.class, it -> it.finalizeExtension(proj));
			extension.forceRealize();
		});
	}

	private NativeTestSuite createNativeTestSuite(String name) {
		return getObjects().newInstance(DefaultNativeTestSuiteComponent.class, NamingScheme.asComponent(name, name).withComponentDisplayName("Test Suite"));
	}
}
