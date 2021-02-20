package dev.nokee.internal.testing;

import lombok.val;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.ConfigurationMatchers.dependencies;
import static dev.nokee.internal.testing.ConfigurationMatchers.forCoordinate;
import static dev.nokee.internal.testing.utils.ConfigurationTestUtils.testConfiguration;
import static dev.nokee.internal.testing.utils.TestUtils.createDependency;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;

class ConfigurationMatchers_DependenciesTest extends AbstractMatcherTest {
	private static final Dependency DEPENDENCY = createDependency("com.example:foo:1.0");

	@Override
	protected Matcher<?> createMatcher() {
		return dependencies(hasItem(DEPENDENCY));
	}

	private static Configuration aConfigurationWithDependencies() {
		val result = testConfiguration();
		result.getDependencies().add(DEPENDENCY);
		result.getDependencies().add(createDependency("com.example:bar:1.0"));
		return result;
	}

	private static Configuration aConfigurationWithDependency(Object notation) {
		val result = testConfiguration();
		result.getDependencies().add(createDependency(notation));
		return result;
	}

	@Test
	void canCheckMatchingDependencies() {
		assertMatches(dependencies(hasItem(DEPENDENCY)), aConfigurationWithDependencies(),
			"matches configuration with 'com.example:foo:1.0' dependency");
	}

	@Test
	void canCheckNonMatchingDependencies() {
		assertDoesNotMatch(dependencies(hasItem(createDependency("foo:bar:4.2"))), aConfigurationWithDependencies(),
			"doesn't match configuration with 'foo:bar:4.2' dependency");
	}

	@Test
	void checkDescription() {
		assertDescription("configuration's dependencies is a collection containing <DefaultExternalModuleDependency{group='com.example', name='foo', version='1.0', configuration='default'}>",
			dependencies(hasItem(DEPENDENCY)));
	}

	@Test
	void checkMismatchDescription() {
		assertMismatchDescription("configuration's dependencies mismatches were: [was <DefaultExternalModuleDependency{group='com.example', name='foo', version='1.0', configuration='default'}>, was <DefaultExternalModuleDependency{group='com.example', name='bar', version='1.0', configuration='default'}>]",
			dependencies(hasItem(createDependency("foo:bar:4.2"))), aConfigurationWithDependencies());
	}

	@Test
	void onlyCheckFirstLevelDependenciesOfConfiguration() {
		assertThat(aConfigurationWithDependency("com.google.guava:guava:28.0-jre"),
			dependencies(contains(forCoordinate("com.google.guava:guava:28.0-jre"))));
	}
}
