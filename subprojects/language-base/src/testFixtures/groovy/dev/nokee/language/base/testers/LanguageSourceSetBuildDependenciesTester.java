/*
 * Copyright 2020-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
