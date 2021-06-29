package dev.nokee.runtime.darwin.internal;

import lombok.val;
import org.gradle.api.Project;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.rootProject;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.isA;

class DarwinRuntimePluginTest {
	private static Project createSubject() {
		val project = rootProject();
		project.getPluginManager().apply(DarwinRuntimePlugin.class);
		return project;
	}

	@Test
	void appliesNativeRuntimePlugin() {
		assertThat(createSubject().getPlugins(), hasItem(isA(DarwinRuntimePlugin.class)));
	}
}
