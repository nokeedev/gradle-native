package dev.nokee.model.internal;

import dev.gradleplugins.grava.testing.util.ProjectTestUtils;
import dev.nokee.model.SelfMutatingNamedDomainObjectProviderTester;
import org.gradle.api.*;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.internal.MutationGuards;
import org.gradle.api.tasks.TaskProvider;
import org.junit.jupiter.api.Nested;

class NamedDomainObjectProviderSelfMutationConfigureStrategyTest {
	private final Project project = ProjectTestUtils.rootProject();

	@Nested
	class TaskProviderTest implements SelfMutatingNamedDomainObjectProviderTester<Task> {
		private final NamedDomainObjectProviderSelfMutationConfigureStrategy strategy = new NamedDomainObjectProviderSelfMutationConfigureStrategy(MutationGuards.of(project.getTasks()));

		@Override
		public TaskProvider<Task> createSubject(String name) {
			return project.getTasks().register(name);
		}

		@Override
		@SuppressWarnings("rawtypes")
		public NamedDomainObjectProvider configure(NamedDomainObjectProvider self, Action action) {
			strategy.configure(self, action);
			return self;
		}
	}

	@Nested
	class ConfigurationContainerTest implements SelfMutatingNamedDomainObjectProviderTester<Configuration> {
		private final NamedDomainObjectProviderSelfMutationConfigureStrategy strategy = new NamedDomainObjectProviderSelfMutationConfigureStrategy(MutationGuards.of(project.getConfigurations()));

		@Override
		public NamedDomainObjectProvider<Configuration> createSubject(String name) {
			return project.getConfigurations().register(name);
		}

		@Override
		@SuppressWarnings("rawtypes")
		public NamedDomainObjectProvider configure(NamedDomainObjectProvider self, Action action) {
			strategy.configure(self, action);
			return self;
		}
	}
}
