package dev.nokee.language.base.testers;

import dev.nokee.language.base.LanguageSourceSet;
import lombok.val;
import org.gradle.api.Buildable;
import org.gradle.api.Task;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.rootProject;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public interface LanguageSourceSetBuildDependenciesTester<T extends LanguageSourceSet> {
	T createSubject();

	@Test
	default void conservesBuildDependenciesOnSourceDirectories() {
		val project = rootProject();
		val buildTask = project.getTasks().create("buildTask");
		val files = project.files(project.file("foo")).builtBy(buildTask);
		val sourceDirectories = createSubject().from(files).getSourceDirectories();
		assertThat(buildDependencies(sourceDirectories), containsInAnyOrder(buildTask));
	}

	@Test
	default void conservesBuildDependenciesOnFileTree() {
		val project = rootProject();
		val buildTask = project.getTasks().create("buildTask");
		val files = project.files(project.file("foo")).builtBy(buildTask);
		val fileTree = createSubject().from(files).getAsFileTree();
		assertThat(buildDependencies(fileTree), containsInAnyOrder(buildTask));
	}

	@Test
	default void hasBuildDependencies() {
		val project = rootProject();
		val buildTask = project.getTasks().create("buildTask");
		val files = project.files(project.file("foo")).builtBy(buildTask);
		assertThat(buildDependencies(createSubject().from(files)), containsInAnyOrder(buildTask));
	}

	@Test
	default void hasNoBuildDependenciesForEmptySourceSet() {
		assertThat(buildDependencies(createSubject()), empty());
	}

	@SuppressWarnings("unchecked")
	static Set<Task> buildDependencies(Buildable buildable) {
		return (Set<Task>)buildable.getBuildDependencies().getDependencies(null);
	}
}
