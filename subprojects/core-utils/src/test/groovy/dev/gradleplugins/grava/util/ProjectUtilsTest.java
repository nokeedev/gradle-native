package dev.gradleplugins.grava.util;

import org.gradle.api.Project;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.createChildProject;
import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.rootProject;
import static dev.gradleplugins.grava.util.ProjectUtils.getPrefixableProjectPath;
import static dev.gradleplugins.grava.util.ProjectUtils.isRootProject;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;

class ProjectUtilsTest {
	@Test
	void canDetectRootProject() {
		Project root = rootProject();
		Project child = createChildProject(root);

		assertAll(
			() -> assertThat(isRootProject(root), is(true)),
			() -> assertThat(isRootProject(child), is(false))
		);
	}

	@Test
	void returnsPrefixableProjectPath() {
		Project root = rootProject();
		Project child = createChildProject(root, "child");

		assertAll(
			() -> assertThat(getPrefixableProjectPath(root), emptyString()),
			() -> assertThat(getPrefixableProjectPath(child), equalTo(":child"))
		);
	}
}
