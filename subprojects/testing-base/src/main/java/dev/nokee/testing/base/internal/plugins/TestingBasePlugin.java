package dev.nokee.testing.base.internal.plugins;

import dev.nokee.model.internal.DomainObjectEventPublisher;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.platform.base.internal.components.*;
import dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin;
import dev.nokee.testing.base.TestSuiteComponent;
import dev.nokee.testing.base.TestSuiteContainer;
import dev.nokee.testing.base.internal.DefaultTestSuiteContainer;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import static dev.nokee.model.internal.DomainObjectIdentifierUtils.withType;

public class TestingBasePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(ComponentModelBasePlugin.class);

		val testSuites = new DefaultTestSuiteContainer(ProjectIdentifier.of(project), project.getExtensions().getByType(ComponentConfigurer.class), project.getExtensions().getByType(DomainObjectEventPublisher.class), project.getExtensions().getByType(ComponentProviderFactory.class), project.getExtensions().getByType(ComponentRepository.class), project.getExtensions().getByType(KnownComponentFactory.class), project.getExtensions().getByType(ComponentInstantiator.class));
		project.getExtensions().add(TestSuiteContainer.class, "testSuites", testSuites);

		project.afterEvaluate(proj -> {
			// Force realize all test suite... until we solve the differing problem.
			project.getExtensions().getByType(ComponentRepository.class).filter(withType(TestSuiteComponent.class));
		});
	}
}
