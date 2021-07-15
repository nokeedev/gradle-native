package dev.nokee.model;

import lombok.val;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionAware;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.rootProject;
import static org.hamcrest.MatcherAssert.assertThat;

class ModelBasePluginTest {
	private Project createSubject() {
		val project = rootProject();
		project.getPluginManager().apply("dev.nokee.model-base");
		return project;
	}

	@Test
	void registersNokeeExtension() {
		assertThat(createSubject(), hasExtensionOf(NokeeExtension.class));
	}

	// TODO: Move to common place
	private static Matcher<ExtensionAware> hasExtensionOf(Class<?> extensionType) {
		return new TypeSafeMatcher<ExtensionAware>() {
			@Override
			public void describeTo(Description description) {
				description.appendText("extension of type " + extensionType);
			}

			@Override
			protected boolean matchesSafely(ExtensionAware item) {
				return item.getExtensions().findByType(extensionType) != null;
			}
		};
	}
}
