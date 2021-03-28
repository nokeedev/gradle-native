package dev.nokee.internal.testing;

import lombok.val;
import org.gradle.api.artifacts.Dependency;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.ConfigurationMatchers.forCoordinate;
import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.*;

class ConfigurationMatchers_ForCoordinateTest extends AbstractMatcherTest {
	@Override
	protected Matcher<?> createMatcher() {
		return forCoordinate("com.example:foo:1.0");
	}

	private static Dependency aProjectDependency() {
		val project = rootProject();
		val target = createChildProject(project, "foo");
		target.setGroup("com.example");
		target.setVersion("1.0");
		return createDependency(target);
	}

	@Test
	void canCheckMatchingProjectCoordinateNotation() {
		assertMatches(forCoordinate("com.example:foo:1.0"), aProjectDependency(),
			"matches project dependency");
	}

	@Test
	void canCheckNonMatchingProjectCoordinateNotation() {
		assertDoesNotMatch(forCoordinate("com.example:bar:1.0"), aProjectDependency(),
			"doesn't match project dependency");
	}

	@Test
	void canCheckCoordinateNotationAsTokens() {
		assertMatches(forCoordinate("com.example", "foo", "1.0"), aProjectDependency(), "matches dependency");
		assertDoesNotMatch(forCoordinate("com.example", "bar", "1.0"), aProjectDependency(), "doesn't match dependency");
	}

	private static Dependency anExternalDependency() {
		return createDependency("com.example:foo:1.0");
	}

	@Test
	void canCheckMatchingExternalCoordinateNotation() {
		assertMatches(forCoordinate("com.example:foo:1.0"), anExternalDependency(),
			"matches external dependency");
	}

	@Test
	void canCheckNonMatchingExternalCoordinateNotation() {
		assertDoesNotMatch(forCoordinate("com.example:bar:1.0"), anExternalDependency(),
			"doesn't match external dependency");
	}

	@Test
	void checkDescription() {
		assertDescription("(a dependency with group \"com.example\" and a dependency with name \"foo\" and a dependency with version \"1.0\")",
			forCoordinate("com.example:foo:1.0"));
	}

	@Test
	void checkMismatchDescription() {
		assertMismatchDescription("a dependency with group \"wrong.group\" but dependency's group was \"com.example\"",
			forCoordinate("wrong.group:foo:1.0"), aProjectDependency());
		assertMismatchDescription("a dependency with name \"wrong-name\" but dependency's name was \"foo\"",
			forCoordinate("com.example:wrong-name:1.0"), aProjectDependency());
		assertMismatchDescription("a dependency with version \"wrong.version\" but dependency's version was \"1.0\"",
			forCoordinate("com.example:foo:wrong.version"), aProjectDependency());
	}
}
