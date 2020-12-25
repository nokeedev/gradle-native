package dev.nokee.testing.base.internal.plugins;

import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.core.ModelSpecs;
import dev.nokee.model.internal.core.NodeRegistration;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin;
import dev.nokee.testing.base.TestSuiteComponent;
import dev.nokee.testing.base.TestSuiteContainer;
import dev.nokee.testing.base.internal.DefaultTestSuiteContainer;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import static dev.nokee.model.internal.BaseNamedDomainObjectContainer.namedContainer;
import static dev.nokee.model.internal.type.ModelType.of;

public class TestingBasePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(ComponentModelBasePlugin.class);

		val modeRegistry = project.getExtensions().getByType(ModelRegistry.class);
		val components = modeRegistry.register(testSuites()).get();
		project.getExtensions().add(TestSuiteContainer.class, "testSuites", components);

		project.afterEvaluate(proj -> {
			// Force realize all test suite... until we solve the differing problem.
			project.getExtensions().getByType(ModelLookup.class).query(ModelSpecs.of(ModelNodes.withType(of(TestSuiteComponent.class)))).forEach(ModelNode::realize);
		});
	}

	private static NodeRegistration<DefaultTestSuiteContainer> testSuites() {
		return namedContainer("testSuites", of(DefaultTestSuiteContainer.class));
	}
}
