/*
 * Copyright 2021 the original author or authors.
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
package dev.nokee.language.base;

import dev.nokee.language.base.internal.SourceSetFactory;
import org.gradle.api.Project;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.ProjectMatchers.buildDependencies;
import static dev.nokee.internal.testing.util.ProjectTestUtils.rootProject;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.emptyIterable;

public class ConfigurableSourceSetEmptyIntegrationTest {
	private final Project project = rootProject();
	private final ConfigurableSourceSet subject = new SourceSetFactory(project.getObjects()).sourceSet();

	@Test
	void hasEmptyFileTreeWhenNoSource() {
		assertThat("file tree should be empty", subject.getAsFileTree(), emptyIterable());
	}

	@Test
	void hasEmptySourceDirectoriesWhenNoSource() {
		assertThat("source directories should be empty", subject.getSourceDirectories(), emptyIterable());
	}

	@Test
	void hasNoFilterPatternByDefault() {
		assertThat(subject.getFilter().getExcludes(), empty());
		assertThat(subject.getFilter().getIncludes(), empty());
	}

	@Test
	void hasNoBuildDependenciesForEmptySourceSet() {
		assertThat(subject, buildDependencies(emptyIterable()));
	}
}
