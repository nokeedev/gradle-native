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
package dev.gradleplugins.grava.testing.util;

import lombok.val;
import org.gradle.api.artifacts.ExternalDependency;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static dev.nokee.internal.testing.util.ProjectTestUtils.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;

class ProjectTestUtilsTest {
	@Test
	void canWorkingObjectFactoryService() {
		assertThat(objectFactory(), notNullValue(ObjectFactory.class));
	}

	@Test
	void canWorkingProviderFactoryService() {
		assertThat(providerFactory(), notNullValue(ProviderFactory.class));
	}

	@Test
	void canCreateDependencyInstance() {
		assertAll(
			() -> assertThat(createDependency(rootProject()), isA(ProjectDependency.class)),
			() -> assertThat(createDependency("com.example:foo:1.0"), isA(ExternalDependency.class))
		);
	}

	@Test
	void canCreateNewRootProject() {
		assertThat(rootProject(), not(equalTo(rootProject())));
	}

	@Test
	void canCreateRootProjectWithSpecificProjectDirectory(@TempDir Path testDirectory) {
		assertAll(
			() -> assertThat(createRootProject(testDirectory.resolve("d0").toFile()).getProjectDir(),
				equalTo(toCanonicalFile(testDirectory.resolve("d0")))),
			() -> assertThat(createRootProject(testDirectory.resolve("d1")).getProjectDir(),
				equalTo(toCanonicalFile(testDirectory.resolve("d1"))))
		);
	}

	@Test
	void canCreateChildProject(@TempDir Path testDirectory) {
		val parent = createRootProject(testDirectory);
		val subject = createChildProject(parent);
		assertAll(
			() -> assertThat(subject.getParent(), equalTo(parent)),
			() -> assertThat(subject.getName(), equalTo("test")),
			() -> assertThat(subject.getProjectDir(), equalTo(toCanonicalFile(testDirectory.resolve("test"))))
		);
	}

	@Test
	void canCreateChildProjectWithSpecificName(@TempDir Path testDirectory) {
		val parent = createRootProject(testDirectory);
		assertAll(
			() -> {
				val subject = createChildProject(parent, "foo");
				assertAll(
					() -> assertThat(subject.getParent(), equalTo(parent)),
					() -> assertThat(subject.getName(), equalTo("foo")),
					() -> assertThat(subject.getProjectDir(), equalTo(toCanonicalFile(testDirectory.resolve("foo"))))
				);
			},
			() -> {
				val subject = createChildProject(parent, "bar");
				assertAll(
					() -> assertThat(subject.getParent(), equalTo(parent)),
					() -> assertThat(subject.getName(), equalTo("bar")),
					() -> assertThat(subject.getProjectDir(), equalTo(toCanonicalFile(testDirectory.resolve("bar"))))
				);
			}
		);
	}

	@Test
	void canCreateChildProjectWithSpecificNameAndProjectDirectory(@TempDir Path testDirectory) {
		val parent = rootProject();
		assertAll(
			() -> {
				val subject = createChildProject(parent, "foo", testDirectory.resolve("d0").toFile());
				assertAll(
					() -> assertThat(subject.getParent(), equalTo(parent)),
					() -> assertThat(subject.getName(), equalTo("foo")),
					() -> assertThat(subject.getProjectDir(), equalTo(toCanonicalFile(testDirectory.resolve("d0"))))
				);
			},
			() -> {
				val subject = createChildProject(parent, "bar", testDirectory.resolve("d1"));
				assertAll(
					() -> assertThat(subject.getParent(), equalTo(parent)),
					() -> assertThat(subject.getName(), equalTo("bar")),
					() -> assertThat(subject.getProjectDir(), equalTo(toCanonicalFile(testDirectory.resolve("d1"))))
				);
			}
		);
	}

	private static File toCanonicalFile(Path path) throws IOException {
		return path.toFile().getCanonicalFile();
	}
}
