package dev.nokee.platform.base.internal.dependencies;

import dev.nokee.platform.base.DependencyBucket;
import lombok.val;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ModuleDependency;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.ConfigurationMatchers.*;
import static dev.nokee.internal.testing.ExecuteWith.*;
import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static dev.nokee.internal.testing.utils.TestUtils.rootProject;
import static dev.gradleplugins.grava.util.ActionUtils.doNothing;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;

interface DependencyBucketTester<T extends DependencyBucket> {
	default T createSubject() {
		return createSubject(rootProject());
	}

	T createSubject(Project project);

	@Test
	default void canAccessProjectConfiguration() {
		val project = rootProject();
		assertThat(createSubject(project).getAsConfiguration(), equalTo(project.getConfigurations().getByName("commonTest")));
	}

	@Test
	default void createsProjectConfigurationMatchingContextAndBucketName() {
		val project = rootProject();
		createSubject(project);
		assertThat(project, hasConfiguration(named("commonTest")));
	}

	@Test
	default void hasBucketName() {
		assertThat(createSubject().getName(), equalTo("test"));
	}

	@Test
	default void canAddDependency() {
		val project = rootProject();
		createSubject(project).addDependency("com.example:foo:4.2");
		assertThat(project, hasConfiguration(dependencies(hasItem(forCoordinate("com.example:foo:4.2")))));
	}

	@Test
	default void canAddDependencyWithConfigurationAction() {
		val project = rootProject();
		createSubject(project).addDependency("com.example:foo:4.2", doNothing());
		assertThat(project, hasConfiguration(dependencies(hasItem(forCoordinate("com.example:foo:4.2")))));
	}

	@Test
	default void canConfigureDependencyBeforeAddingIt() {
		ExecutionResult<ModuleDependency> executionResult = executeWith(action(action -> createSubject().addDependency("com.example:foo:4.2", action)));
		assertThat(executionResult, calledOnceWith(forCoordinate("com.example:foo:4.2")));
	}
}
