package dev.nokee.model.internal;

import dev.nokee.model.SelfMutatingNamedDomainObjectProviderTester;
import dev.nokee.model.TestProjection;
import lombok.val;
import org.gradle.api.*;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.tasks.TaskProvider;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.rootProject;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DefaultNamedDomainObjectProviderSelfMutationDecoratorTest {
	private final Project project = rootProject();
	private final NamedDomainObjectProviderSelfMutationDecoratorFactory factory = new DefaultNamedDomainObjectSelfMutationDecoratorFactory();

	@Test
	void decoratedTaskProvidersAreStillTaskProviders() {
		val decorator = factory.forContainer(project.getTasks());
		val subject = decorator.decorate(project.getTasks().register("skrg"));
		assertThat("ensure the decorator creates equivalent domain object providers",
			subject, isA(TaskProvider.class));
	}

	@Test
	void decoratedNamedDomainObjectProvidersAreNotTaskProviders() {
		val decorator = factory.forContainer(project.getConfigurations());
		val subject = decorator.decorate(project.getConfigurations().register("kidj"));
		assertThat("ensure the decorator wasn't lazily implemented which could have side-effects",
			subject, not(isA(TaskProvider.class)));
	}

	abstract class NamedDomainObjectProviderDecoratorContainerIntegrationTester<T> {
		private final NamedDomainObjectProviderDecorator decorator = factory.forContainer(container());

		public abstract NamedDomainObjectContainer<T> container();

		public NamedDomainObjectProviderDecorator subject() {
			return decorator;
		}

		@Test
		void returnsNamedDomainObjectProviderUponDecoration() {
			assertThat(decorator.decorate(container().register("gkwd")), isA(NamedDomainObjectProvider.class));
		}

		@Test
		void throwsExceptionIfDecoratingProviderIsNotCompatibleWithDecorator() {
			val unrelatedContainer = project.container(TestProjection.class);
			assertThrows(RuntimeException.class, () -> decorator.decorate(unrelatedContainer.register("kdji")));
		}

		@Nested
		class SelfMutationTest implements SelfMutatingNamedDomainObjectProviderTester<T> {
			@Override
			public NamedDomainObjectProvider<T> createSubject(String name) {
				return decorator.decorate(container().register(name));
			}

			@Override
			@SuppressWarnings({"rawtypes", "unchecked"})
			public NamedDomainObjectProvider configure(NamedDomainObjectProvider self, Action action) {
				self.configure(action);
				return self;
			}
		}
	}

	@Nested
	class TaskContainerTest extends NamedDomainObjectProviderDecoratorContainerIntegrationTester<Task> {
		@Override
		public NamedDomainObjectContainer<Task> container() {
			return project.getTasks();
		}

		@Test
		void returnsDecoratedTaskProvider() {
			assertThat(subject().decorate(project.getTasks().register("fgwy")), isA(TaskProvider.class));
		}
	}

	@Nested
	class ConfigurationContainerTest extends NamedDomainObjectProviderDecoratorContainerIntegrationTester<Configuration> {
		@Override
		public NamedDomainObjectContainer<Configuration> container() {
			return project.getConfigurations();
		}
	}
}
