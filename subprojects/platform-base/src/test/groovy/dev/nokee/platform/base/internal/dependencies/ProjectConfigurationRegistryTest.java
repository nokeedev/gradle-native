package dev.nokee.platform.base.internal.dependencies;

import com.google.common.testing.NullPointerTester;
import lombok.val;
import org.apache.commons.lang3.mutable.MutableObject;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.attributes.Usage;
import org.gradle.api.model.ObjectFactory;
import org.junit.jupiter.api.Test;
import spock.lang.Subject;

import java.util.function.Consumer;

import static dev.nokee.internal.testing.ExecuteWith.*;
import static dev.nokee.internal.testing.utils.TestUtils.rootProject;
import static dev.nokee.platform.base.internal.dependencies.ProjectConfigurationRegistry.forProject;
import static dev.nokee.platform.base.internal.dependencies.ProjectConfigurationUtils.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Subject(ProjectConfigurationRegistry.class)
class ProjectConfigurationRegistryTest {
	private static final Consumer<Configuration> DO_SOMETHING = t -> {};

	@Test
	void canCallbackWithObjectFactoryInstance() {
		val capturedConfiguration = new MutableObject<Configuration>();
		val capturedObjectFactory = new MutableObject<ObjectFactory>();
		val project = rootProject();
		forProject(project).createIfAbsent("configuration-0", withObjectFactory((configuration, objectFactory) -> {
			capturedConfiguration.setValue(configuration);
			capturedObjectFactory.setValue(objectFactory);
		}));
		assertThat(capturedConfiguration.getValue(), equalTo(project.getConfigurations().getByName("configuration-0")));
		assertThat(capturedObjectFactory.getValue(), equalTo(project.getObjects()));
	}

	@Test
	void createsMissingConfiguration() {
		val project = rootProject();
		val configuration = forProject(project).createIfAbsent("configuration-1", DO_SOMETHING);
		assertThat(configuration, equalTo(project.getConfigurations().getByName("configuration-1")));
	}

	@Test
	void executesActionForMissingConfiguration() {
		val execution = executeWith(consumer(it -> forProject(rootProject()).createIfAbsent("configuration-1", it)));
		assertThat(execution, calledOnce());
	}

	@Test
	void returnsExistingConfiguration() {
		val project = rootProject();
		val expected = project.getConfigurations().create("configuration-2");
		val actual = forProject(project).createIfAbsent("configuration-2", DO_SOMETHING);
		assertThat(actual, equalTo(expected));
	}

	@Test
	void doesNotExecuteActionForExistingConfiguration() {
		val execution = executeWith(consumer(it -> forProject(projectWithExistingConfiguration()).createIfAbsent("existing", it)));
		assertThat(execution, neverCalled());
	}

	@Test
	void doesNotTriggerConfigurationContainerRulesWhenConfigurationIsAbsent() {
		assertDoesNotThrow(() -> forProject(projectWithThrowingConfigurationRule()).createIfAbsent("configuration-3", DO_SOMETHING));
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

	@Test
	void throwsExceptionWhenExistingConfigurationIsNotConsumable() {
		assertThrows(IllegalStateException.class,
			() -> forProject(projectWithExistingConfiguration()).createIfAbsent("existing", asConsumable()));
	}

	@Test
	void throwsExceptionWhenExistingConfigurationIsNotResolvable() {
		assertThrows(IllegalStateException.class,
			() -> forProject(projectWithExistingConfiguration()).createIfAbsent("existing", asResolvable()));
	}

	@Test
	void throwsExceptionWhenExistingConfigurationIsNotDeclarable() {
		assertThrows(IllegalStateException.class,
			() -> forProject(projectWithExistingConfiguration()).createIfAbsent("existing", asDeclarable()));
	}

	private static Project projectWithExistingConfiguration() {
		val project = rootProject();
		project.getConfigurations().create("existing");
		return project;
	}

	@Test
	void throwsExceptionWhenExistingConfigurationHasNotTheAttribute() {
		assertThrows(IllegalStateException.class,
			() -> forProject(projectWithExistingConfiguration()).createIfAbsent("existing", forUsage("some-usage")));
	}

	@Test
	void throwsExceptionWhenExistingConfigurationHasWrongAttributeValue() {
		val project = rootProject();
		project.getConfigurations().create("existing", configuration -> configuration.getAttributes().attribute(Usage.USAGE_ATTRIBUTE, project.getObjects().named(Usage.class, "some-other-usage")));
		assertThrows(IllegalStateException.class,
			() -> forProject(project).createIfAbsent("existing", forUsage("some-usage")));
	}
}
