package dev.nokee.model;

import lombok.val;
import org.gradle.api.Project;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.rootProject;
import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static dev.nokee.internal.testing.ProjectMatchers.extensions;
import static dev.nokee.internal.testing.ProjectMatchers.publicType;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;

class ModelBasePluginTest {
	private Project createSubject() {
		val project = rootProject();
		project.getPluginManager().apply("dev.nokee.model-base");
		return project;
	}

	@Test
	void registersNokeeExtension() {
		assertThat(createSubject(), extensions(hasItem(allOf(named("nokee"), publicType(NokeeExtension.class)))));
	}
}
