package dev.nokee.internal.testing;

import lombok.val;
import org.gradle.api.Project;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.rootProject;
import static dev.nokee.internal.testing.ProjectMatchers.hasPlugin;
import static org.hamcrest.MatcherAssert.assertThat;

class ProjectMatchers_HasPluginIdTest extends AbstractMatcherTest {
	@Override
	protected Matcher<?> createMatcher() {
		return hasPlugin("java-base");
	}

	private static Project aProjectWithJavaPlugin() {
		val result = rootProject();
		result.getPluginManager().apply("java");
		return result;
	}

	@Test
	void canCheckMatchingConfigurations() {
		assertMatches(hasPlugin("java"), aProjectWithJavaPlugin(),
			"matches project with applied plugin id 'java'");
	}

	@Test
	void canCheckNonMatchingDependencies() {
		assertDoesNotMatch(hasPlugin("groovy"), aProjectWithJavaPlugin(),
			"doesn't match project with applied plugin id 'groovy'");
	}

	@Test
	void checkDescription() {
		assertDescription("a plugin aware object has plugin \"java\" applied",
			hasPlugin("java"));
	}

	@Test
	void checkMismatchDescription() {
		assertMismatchDescription("plugin \"groovy\" is not applied to <root project 'test'>",
			hasPlugin("groovy"), aProjectWithJavaPlugin());
	}

	@Test
	void canCheckPluginAppliedToProject() {
		assertThat(aProjectWithJavaPlugin(), hasPlugin("java"));
	}
}
