package dev.nokee.testing.nativebase.internal.plugins;

import dev.nokee.language.base.internal.LanguageSourceSetRegistry;
import dev.nokee.language.base.internal.LanguageSourceSetRepository;
import dev.nokee.language.base.internal.LanguageSourceSetViewFactory;
import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.internal.DomainObjectEventPublisher;
import dev.nokee.model.internal.NameAwareDomainObjectIdentifier;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.base.internal.ComponentName;
import dev.nokee.platform.base.internal.binaries.BinaryViewFactory;
import dev.nokee.platform.base.internal.tasks.TaskRegistry;
import dev.nokee.platform.base.internal.tasks.TaskViewFactory;
import dev.nokee.platform.base.internal.variants.VariantRepository;
import dev.nokee.platform.base.internal.variants.VariantViewFactory;
import dev.nokee.testing.base.TestSuiteContainer;
import dev.nokee.testing.base.internal.plugins.TestingBasePlugin;
import dev.nokee.testing.nativebase.NativeTestSuite;
import dev.nokee.testing.nativebase.internal.DefaultNativeTestSuiteComponent;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class NativeUnitTestingPlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply("lifecycle-base");
		project.getPluginManager().apply(TestingBasePlugin.class);

		val testSuites = project.getExtensions().getByType(TestSuiteContainer.class);
		testSuites.registerFactory(DefaultNativeTestSuiteComponent.class, name -> createNativeTestSuite(name, project));
		testSuites.registerBinding(NativeTestSuite.class, DefaultNativeTestSuiteComponent.class);

		project.afterEvaluate(proj -> {
			// TODO: We delay as late as possible to "fake" a finalize action.
			testSuites.configureEach(DefaultNativeTestSuiteComponent.class, it -> {
				it.finalizeExtension(proj);
			});
		});
	}

	private DefaultNativeTestSuiteComponent createNativeTestSuite(DomainObjectIdentifier name, Project project) {
		val identifier = ComponentIdentifier.of(ComponentName.of(((NameAwareDomainObjectIdentifier)name).getName().toString()), DefaultNativeTestSuiteComponent.class, ProjectIdentifier.of(project));
		return new DefaultNativeTestSuiteComponent(identifier, project.getObjects(), project.getProviders(), project.getTasks(), project.getConfigurations(), project.getDependencies(), project.getExtensions().getByType(DomainObjectEventPublisher.class), project.getExtensions().getByType(VariantViewFactory.class), project.getExtensions().getByType(VariantRepository.class), project.getExtensions().getByType(BinaryViewFactory.class), project.getExtensions().getByType(TaskRegistry.class), project.getExtensions().getByType(TaskViewFactory.class), project.getExtensions().getByType(LanguageSourceSetRepository.class), project.getExtensions().getByType(LanguageSourceSetViewFactory.class), project.getExtensions().getByType(LanguageSourceSetRegistry.class));
	}
}
