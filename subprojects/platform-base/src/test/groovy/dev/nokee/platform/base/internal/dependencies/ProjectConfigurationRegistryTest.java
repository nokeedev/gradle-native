package dev.nokee.platform.base.internal.dependencies;

import com.google.common.testing.NullPointerTester;
import lombok.val;
import org.apache.commons.lang3.mutable.MutableObject;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.model.ObjectFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mockito;
import spock.lang.Subject;

import java.util.function.Consumer;

import static dev.nokee.internal.testing.ExecuteWith.*;
import static dev.nokee.internal.testing.utils.TestUtils.rootProject;
import static dev.nokee.platform.base.internal.dependencies.ProjectConfigurationRegistry.forProject;
import static dev.nokee.platform.base.internal.dependencies.ProjectConfigurationUtils.withObjectFactory;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;

@Subject(ProjectConfigurationRegistry.class)
class ProjectConfigurationRegistryTest {
	private static final Consumer<Configuration> DO_SOMETHING = t -> {};

	@ParameterizedTest
	@EnumSource(CreateMethod.class)
	void canCallbackWithObjectFactoryInstance(CreateMethod create) {
		val capturedConfiguration = new MutableObject<Configuration>();
		val capturedObjectFactory = new MutableObject<ObjectFactory>();
		val project = rootProject();
		create.invoke(forProject(project), "configuration-0", withObjectFactory((configuration, objectFactory) -> {
			capturedConfiguration.setValue(configuration);
			capturedObjectFactory.setValue(objectFactory);
		}));
		assertThat(capturedConfiguration.getValue(), equalTo(project.getConfigurations().getByName("configuration-0")));
		assertThat(capturedObjectFactory.getValue(), equalTo(project.getObjects()));
	}

	@ParameterizedTest
	@EnumSource(CreateMethod.class)
	void createsMissingConfiguration(CreateMethod create) {
		val project = rootProject();
		val configuration = create.invoke(forProject(project), "configuration-1", DO_SOMETHING);
		assertThat(configuration, equalTo(project.getConfigurations().getByName("configuration-1")));
	}

	@ParameterizedTest
	@EnumSource(CreateMethod.class)
	void executesActionForMissingConfiguration(CreateMethod create) {
		val execution = executeWith(consumer(it -> create.invoke(forProject(rootProject()), "configuration-1", it)));
		assertThat(execution, calledOnce());
	}

	@Test
	void doesNotExecutesActionForMissingConfigurationWhenRegisteringOnly() {
		val execution = executeWith(consumer(it -> forProject(rootProject()).registerIfAbsent("configuration-1", it)));
		assertThat(execution, neverCalled());
	}

	@ParameterizedTest
	@EnumSource(CreateMethod.class)
	void returnsExistingConfiguration(CreateMethod create) {
		val project = rootProject();
		val expected = project.getConfigurations().create("configuration-2");
		val actual = create.invoke(forProject(project), "configuration-2", DO_SOMETHING);
		assertThat(actual, equalTo(expected));
	}

	@Test
	void doesNotRealizeExistingConfigurationWhenRegistering() {
		val execution = executeWith(action(it -> {
			val project = rootProject();
			project.getConfigurations().register("existing", it);
			forProject(project).registerIfAbsent("existing", DO_SOMETHING);
		}));
		assertThat(execution, neverCalled());
	}

	@ParameterizedTest
	@EnumSource(CreateMethod.class)
	void doesNotExecuteActionForExistingConfiguration(CreateMethod create) {
		val execution = executeWith(consumer(it -> create.invoke(forProject(projectWithExistingConfiguration()), "existing", it)));
		assertThat(execution, neverCalled());
	}

	@Test
	void doesNotExecuteActionWhenRegisteringNewConfiguration() {
		val execution = executeWith(consumer(it -> forProject(rootProject()).registerIfAbsent("existing", it)));
		assertThat(execution, neverCalled());
	}

	@ParameterizedTest
	@EnumSource(CreateMethod.class)
	void doesNotTriggerConfigurationContainerRulesWhenConfigurationIsAbsent(CreateMethod create) {
		assertDoesNotThrow(() -> create.invoke(forProject(projectWithThrowingConfigurationRule()), "configuration-3", DO_SOMETHING));
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
		CreateIfAbsent {
			@Override
			Configuration invoke(ProjectConfigurationRegistry registry, String name, Consumer<? super Configuration> action) {
				return registry.createIfAbsent(name, action);
			}
		},
		RegisterIfAbsent {
			@Override
			Configuration invoke(ProjectConfigurationRegistry registry, String name, Consumer<? super Configuration> action) {
				return registry.registerIfAbsent(name, action).get();
			}
		};

		abstract Configuration invoke(ProjectConfigurationRegistry registry, String name, Consumer<? super Configuration> action);
	}

	@Test
	void assertExistingConfigurationWhenCreating() {
		val action = Mockito.mock(AssertableConsumer.class);
		val configuration = forProject(projectWithExistingConfiguration()).createIfAbsent("existing", action);
		Mockito.verify(action).assertValue(configuration);
	}

	@Test
	void doesNotEagerlyAssertExistingConfigurationWhenRegistering() {
		val action = Mockito.mock(AssertableConsumer.class);
		forProject(projectWithExistingConfiguration()).registerIfAbsent("existing", action);
		Mockito.verify(action, never()).assertValue(any());
	}

	@Test
	void assertExistingConfigurationWhenRegisteredConfigurationIsRealized() {
		val action = Mockito.mock(AssertableConsumer.class);
		val configuration = forProject(projectWithExistingConfiguration()).registerIfAbsent("existing", action).get();
		Mockito.verify(action).assertValue(configuration);
	}

	private interface AssertableConsumer extends Consumer<Configuration>, ProjectConfigurationUtils.Assertable<Configuration> {}
}
