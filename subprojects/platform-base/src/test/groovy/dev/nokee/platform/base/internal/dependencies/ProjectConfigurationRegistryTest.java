package dev.nokee.platform.base.internal.dependencies;

import com.google.common.testing.NullPointerTester;
import dev.nokee.utils.ActionUtils;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.TaskContainer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mockito;
import spock.lang.Subject;

import static dev.nokee.internal.testing.ExecuteWith.*;
import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.rootProject;
import static dev.nokee.platform.base.internal.dependencies.ProjectConfigurationActions.withObjectFactory;
import static dev.nokee.platform.base.internal.dependencies.ProjectConfigurationActions.withTaskContainer;
import static dev.nokee.platform.base.internal.dependencies.ProjectConfigurationRegistry.forProject;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@Subject(ProjectConfigurationRegistry.class)
class ProjectConfigurationRegistryTest {
	private static final Action<Configuration> DO_SOMETHING = t -> {};

	@ParameterizedTest
	@EnumSource(CreateMethod.class)
	void hasAccessToObjectFactoryDuringConfigurationCreation(CreateMethod create) {
		assertThat(executeWith(biConsumer(action -> create.invoke(forProject(rootProject()), "c-0", withObjectFactory(action)))),
			calledOnceWith(allOf(firstArgumentOf(named("c-0")), secondArgumentOf(isA(ObjectFactory.class)))));
	}

	@ParameterizedTest
	@EnumSource(CreateMethod.class)
	void hasAccessToTaskContainerDuringConfigurationCreation(CreateMethod create) {
		assertThat(executeWith(biConsumer(action -> create.invoke(forProject(rootProject()), "c-1", withTaskContainer(action)))),
			calledOnceWith(allOf(firstArgumentOf(named("c-1")), secondArgumentOf(isA(TaskContainer.class)))));
	}

	@ParameterizedTest
	@EnumSource(CreateMethod.class)
	void createsMissingConfiguration(CreateMethod create) {
		val project = rootProject();
		val configuration = create.invoke(forProject(project), "c-2");
		assertThat(configuration, equalTo(project.getConfigurations().getByName("c-2")));
	}

	@ParameterizedTest
	@EnumSource(CreateMethod.class)
	void executesActionForMissingConfiguration(CreateMethod create) {
		val execution = executeWith(action(it -> create.invoke(forProject(rootProject()), "c-3", it)));
		assertThat(execution, calledOnce());
	}

	@Test
	void returnsExistingConfiguration() {
		val project = rootProject();
		val expected = project.getConfigurations().create("c-5");
		val actual = forProject(project).createIfAbsent("c-5", DO_SOMETHING);
		assertThat(actual, equalTo(expected));
	}

	@Test
	void doesNotExecuteActionForExistingConfiguration() {
		val execution = executeWith(action(it -> forProject(projectWithExistingConfiguration()).createIfAbsent("existing", it)));
		assertThat(execution, neverCalled());
	}

	@ParameterizedTest
	@EnumSource(CreateMethod.class)
	void doesNotTriggerConfigurationContainerRulesWhenConfigurationIsAbsent(CreateMethod create) {
		assertDoesNotThrow(() -> create.invoke(forProject(projectWithThrowingConfigurationRule()), "configuration-3"));
	}

	private static Project projectWithThrowingConfigurationRule() {
		val project = rootProject();
		project.getConfigurations().addRule("always throws", name -> { throw new UnsupportedOperationException(); });
		return project;
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkNulls() {
		new NullPointerTester().testAllPublicStaticMethods(ProjectConfigurationRegistry.class);
	}

	private static Project projectWithExistingConfiguration() {
		val project = rootProject();
		project.getConfigurations().register("existing");
		return project;
	}

	private enum CreateMethod {
		Create {
			@Override
			Configuration invoke(ProjectConfigurationRegistry registry, String name, Action<? super Configuration> action) {
				return registry.create(name, action);
			}
		},
		CreateIfAbsent {
			@Override
			Configuration invoke(ProjectConfigurationRegistry registry, String name, Action<? super Configuration> action) {
				return registry.createIfAbsent(name, action);
			}
		};

		Configuration invoke(ProjectConfigurationRegistry registry, String name) {
			return invoke(registry, name, DO_SOMETHING);
		}

		abstract Configuration invoke(ProjectConfigurationRegistry registry, String name, Action<? super Configuration> action);
	}

	@Test
	void assertExistingConfigurationWhenCreating() {
		val action = Mockito.mock(AssertableConsumer.class);
		val configuration = forProject(projectWithExistingConfiguration()).createIfAbsent("existing", action);
		Mockito.verify(action).assertValue(configuration);
	}

	private interface AssertableConsumer extends ActionUtils.Action<Configuration>, ProjectConfigurationActions.Assertable<Configuration> {}
}
