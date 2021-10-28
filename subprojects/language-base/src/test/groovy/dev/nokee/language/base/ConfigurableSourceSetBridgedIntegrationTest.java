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
package dev.nokee.language.base;

import dev.nokee.internal.testing.FileSystemWorkspace;
import dev.nokee.language.base.internal.SourceSetFactory;
import dev.nokee.language.base.testers.ConfigurableSourceSetIntegrationTester;
import lombok.val;
import org.gradle.api.Project;
import org.gradle.api.file.SourceDirectorySet;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static dev.nokee.internal.testing.FileSystemWorkspace.newFiles;
import static dev.nokee.internal.testing.util.ProjectTestUtils.rootProject;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

class ConfigurableSourceSetBridgedIntegrationTest extends ConfigurableSourceSetIntegrationTester {
	private final Project project = rootProject();
	private final SourceDirectorySet sourceSet = project.getObjects().sourceDirectorySet("test", "test");
	private final ConfigurableSourceSet subject = new SourceSetFactory(project.getObjects()).bridgedSourceSet(sourceSet);

	@Override
	public ConfigurableSourceSet subject() {
		return subject;
	}

	@Override
	public File getTemporaryDirectory() throws IOException {
		return project.getProjectDir();
	}

	@Test
	void bridgedSourceSetValuesTakePrecedenceOverOurValue() throws IOException {
		val a = new FileSystemWorkspace(getTemporaryDirectory());
		sourceSet.srcDir(newFiles(a.newDirectory("src/main/java")));
		assertThat(subject.getAsFileTree(), containsInAnyOrder(a.file("src/main/java/f1"), a.file("src/main/java/f2")));
		assertThat(subject.getSourceDirectories(), containsInAnyOrder(a.file("src/main/java")));
	}

	@Test // TODO: ignore convention when source set already have data upon bridging
	void conventionOnNonEmptyBridgedSourceSetAreIgnored() throws IOException {
		val a = new FileSystemWorkspace(getTemporaryDirectory());
		sourceSet.srcDir(newFiles(a.newDirectory("src/main/java")));
		subject.convention(a.file("srcs"));
		assertThat(subject.getAsFileTree(), containsInAnyOrder(a.file("src/main/java/f1"), a.file("src/main/java/f2")));
		assertThat(subject.getSourceDirectories(), containsInAnyOrder(a.file("src/main/java")));
		assertThat(sourceSet.getAsFileTree(), containsInAnyOrder(a.file("src/main/java/f1"), a.file("src/main/java/f2")));
		assertThat(sourceSet.getSourceDirectories(), containsInAnyOrder(a.file("src/main/java")));
	}

	@Test // TOOD: use convention when source set is empty upon bridging
	void conventionOnEmptyBridgedSourceSetIsUsed() throws IOException {
		val a = new FileSystemWorkspace(getTemporaryDirectory());
		subject.convention(newFiles(a.newDirectory("srcs")));
		assertThat(subject.getAsFileTree(), containsInAnyOrder(a.file("srcs/f1"), a.file("srcs/f2")));
		assertThat(subject.getSourceDirectories(), containsInAnyOrder(a.file("srcs")));
		assertThat(sourceSet.getAsFileTree(), containsInAnyOrder(a.file("srcs/f1"), a.file("srcs/f2")));
		assertThat(sourceSet.getSourceDirectories(), containsInAnyOrder(a.file("srcs")));
	}

	// TODO: convention ignored when data is added after bridging but didn't have data when bridging
	@Test
	void ignoresConventionBridgedSourceSetIsUsed() throws IOException {
		val a = new FileSystemWorkspace(getTemporaryDirectory());
		subject.convention(newFiles(a.newDirectory("srcs")));
		sourceSet.srcDir(newFiles(a.newDirectory("src/main/java")));

		assertThat(subject.getAsFileTree(), containsInAnyOrder(a.file("src/main/java/f1"), a.file("src/main/java/f2")));
		assertThat(subject.getSourceDirectories(), containsInAnyOrder(a.file("src/main/java")));
		assertThat(sourceSet.getAsFileTree(), containsInAnyOrder(a.file("src/main/java/f1"), a.file("src/main/java/f2")));
		assertThat(sourceSet.getSourceDirectories(), containsInAnyOrder(a.file("src/main/java")));
	}
}
