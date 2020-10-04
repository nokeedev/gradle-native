package dev.nokee.testing.nativebase.internal.plugins;

import dev.nokee.language.c.internal.CSourceSet;
import dev.nokee.language.cpp.internal.CppSourceSet;
import dev.nokee.language.objectivec.internal.ObjectiveCSourceSet;
import dev.nokee.language.objectivecpp.internal.ObjectiveCppSourceSet;
import dev.nokee.language.swift.internal.SwiftSourceSet;
import dev.nokee.model.internal.DomainObjectEventPublisher;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.base.internal.ComponentName;
import dev.nokee.platform.base.internal.ProjectIdentifier;
import dev.nokee.platform.base.internal.binaries.BinaryViewFactory;
import dev.nokee.platform.base.internal.tasks.TaskRegistry;
import dev.nokee.platform.base.internal.variants.VariantRepository;
import dev.nokee.platform.base.internal.variants.VariantViewFactory;
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
		extension.registerFactory(NativeTestSuite.class, DefaultNativeTestSuiteComponent.class, name -> {
			return createNativeTestSuite(ComponentIdentifier.of(ComponentName.of(name), DefaultNativeTestSuiteComponent.class, ProjectIdentifier.of(project)), project);
		});

		extension.whenElementKnown(DefaultNativeTestSuiteComponent.class, knownTestSuite -> {
			knownTestSuite.configure(testSuite -> {
				// TODO: Move these generic source set creation to the respective language plugin
				if (project.getPluginManager().hasPlugin("dev.nokee.c-language")) {
					testSuite.getSourceCollection().add(getObjects().newInstance(CSourceSet.class, "c").from("src/" + testSuite.getIdentifier().getName().get() + "/c"));
				}
				if (project.getPluginManager().hasPlugin("dev.nokee.cpp-language")) {
					testSuite.getSourceCollection().add(getObjects().newInstance(CppSourceSet.class, "cpp").from("src/" + testSuite.getIdentifier().getName().get() + "/cpp"));
				}
				if (project.getPluginManager().hasPlugin("dev.nokee.objective-c-language")) {
					testSuite.getSourceCollection().add(getObjects().newInstance(ObjectiveCSourceSet.class, "objc").from("src/" + testSuite.getIdentifier().getName().get() + "/objc"));
				}
				if (project.getPluginManager().hasPlugin("dev.nokee.objective-cpp-language")) {
					testSuite.getSourceCollection().add(getObjects().newInstance(ObjectiveCppSourceSet.class, "objcpp").from("src/" + testSuite.getIdentifier().getName().get() + "/objcpp"));
				}
				if (project.getPluginManager().hasPlugin("dev.nokee.swift-language")) {
					testSuite.getSourceCollection().add(getObjects().newInstance(SwiftSourceSet.class, "swift").from("src/" + testSuite.getIdentifier().getName().get() + "/swift"));
				}
			});
		});

		project.afterEvaluate(proj -> {
			// TODO: We delay as late as possible to "fake" a finalize action.
			extension.configureEach(DefaultNativeTestSuiteComponent.class, it -> it.finalizeExtension(proj));
			extension.forceRealize();
		});
	}

	private NativeTestSuite createNativeTestSuite(ComponentIdentifier<DefaultNativeTestSuiteComponent> identifier, Project project) {
		return getObjects().newInstance(DefaultNativeTestSuiteComponent.class, identifier, project.getExtensions().getByType(DomainObjectEventPublisher.class), project.getExtensions().getByType(VariantViewFactory.class), project.getExtensions().getByType(VariantRepository.class), project.getExtensions().getByType(BinaryViewFactory.class), project.getExtensions().getByType(TaskRegistry.class));
	}
}
