package dev.nokee.internal.testing;

import lombok.val;
import org.gradle.api.Project;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.ConfigurationMatchers.configurations;
import static dev.nokee.internal.testing.ConfigurationMatchers.hasConfiguration;
import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static dev.nokee.internal.testing.utils.TestUtils.rootProject;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;

class ConfigurationMatchers_ConfigurationsTest extends AbstractMatcherTest {
	@Override
	protected Matcher<?> createMatcher() {
		return configurations(hasItem(named("foo")));
	}

	private static Project aProjectWithConfigurations() {
		val result = rootProject();
		result.getConfigurations().create("test");
		return result;
	}

	@Test
	void canCheckMatchingConfigurations() {
		assertMatches(configurations(hasItem(named("test"))), aProjectWithConfigurations(),
			"matches project with configuration named 'test'");
	}

	@Test
	void canCheckNonMatchingDependencies() {
		assertDoesNotMatch(configurations(hasItem(named("nonExistent"))), aProjectWithConfigurations(),
			"doesn't match project with configuration named 'nonExistent'");
	}

	@Test
	void checkDescription() {
		assertDescription("project's configurations is a collection containing an object named \"test\"",
			configurations(hasItem(named("test"))));
	}

	@Test
	void checkMismatchDescription() {
		assertMismatchDescription("project's configurations mismatches were: [the object's name was \"test\"]",
			configurations(hasItem(named("nonExistent"))), aProjectWithConfigurations());
	}

	@Test
	void canCheckConfigurationFromProject() {
		assertThat(aProjectWithConfigurations(), hasConfiguration(named("test")));
	}

}
