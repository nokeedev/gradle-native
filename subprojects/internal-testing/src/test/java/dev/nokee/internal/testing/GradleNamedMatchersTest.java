package dev.nokee.internal.testing;

import lombok.val;
import org.gradle.api.Named;
import org.hamcrest.Description;
import org.hamcrest.StringDescription;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.objectFactory;
import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.rootProject;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasToString;

class GradleNamedMatchersTest {
	@Test
	void givesSensibleErrorMessage() {
		assertThat(description(it -> named("foo").describeMismatch(objectFactory().named(Named.class, "bar"), it)),
			hasToString("the object's name was \"bar\""));
	}

	@Test
	void canCheckNamedObject() {
		assertThat(objectFactory().named(Named.class, "aNamedObject"), named("aNamedObject"));
	}

	@Test
	void canCheckConfigurationObject() {
		assertThat(rootProject().getConfigurations().create("aConfiguration"), named("aConfiguration"));
	}

	@Test
	void canCheckTaskObject() {
		assertThat(rootProject().getTasks().create("aTask"), named("aTask"));
	}

	@Test
	void canCheckNamedUsingMatcher() {
		assertThat(objectFactory().named(Named.class, "somethingSomethingDarkSide"), named(containsString("Dark")));
	}

	private static Description description(Consumer<? super Description> action) {
		val description = new StringDescription();
		action.accept(description);
		return description;
	}
}
