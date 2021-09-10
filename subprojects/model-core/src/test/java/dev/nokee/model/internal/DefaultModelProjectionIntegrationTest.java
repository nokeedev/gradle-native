package dev.nokee.model.internal;

import dev.nokee.internal.testing.PluginRequirement;
import dev.nokee.model.SelfMutatingNamedDomainObjectProviderTester;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.tasks.TaskProvider;
import org.junit.jupiter.api.Nested;

import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.rootProject;
import static dev.nokee.model.NokeeExtension.nokee;

@PluginRequirement.Require(id = "dev.nokee.model-base")
class DefaultModelProjectionIntegrationTest {
	private final Project project = rootProject();

	@Nested
	@SuppressWarnings({"rawtypes", "unchecked"})
	class SelfMutateConfigurationProjectionTest implements SelfMutatingNamedDomainObjectProviderTester<Configuration> {
		@Override
		public NamedDomainObjectProvider<Configuration> createSubject(String name) {
			return nokee(project).getModelRegistry().getRoot().newChildNode(name)
				.newProjection(builder -> builder.type(Configuration.class)).get(NamedDomainObjectProvider.class);
		}

		@Override
		public NamedDomainObjectProvider configure(NamedDomainObjectProvider self, Action action) {
			self.configure(action);
			return self;
		}
	}

	@Nested
	@SuppressWarnings({"rawtypes", "unchecked"})
	class SelfMutateTaskProjectionTest implements SelfMutatingNamedDomainObjectProviderTester<Task> {
		@Override
		public NamedDomainObjectProvider<Task> createSubject(String name) {
			return nokee(project).getModelRegistry().getRoot().newChildNode(name)
				.newProjection(builder -> builder.type(Task.class)).get(TaskProvider.class);
		}

		@Override
		public NamedDomainObjectProvider configure(NamedDomainObjectProvider self, Action action) {
			self.configure(action);
			return self;
		}
	}
}
