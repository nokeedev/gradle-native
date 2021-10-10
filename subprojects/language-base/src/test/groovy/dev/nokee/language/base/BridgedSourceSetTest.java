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

import dev.nokee.internal.testing.util.ProjectTestUtils;
import dev.nokee.internal.testing.FileSystemWorkspace;
import dev.nokee.language.base.internal.BridgedLanguageSourceSetProjection;
import dev.nokee.language.base.testers.LanguageSourceSetTester;
import lombok.val;
import org.gradle.api.file.SourceDirectorySet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import spock.lang.Subject;

import java.io.File;
import java.io.IOException;

import static dev.nokee.internal.testing.FileSystemWorkspace.newFiles;
import static dev.nokee.internal.testing.util.ProjectTestUtils.createRootProject;
import static dev.nokee.internal.testing.util.ProjectTestUtils.objectFactory;
import static dev.nokee.language.base.internal.plugins.LanguageBasePlugin.bridgeSourceSet;
import static dev.nokee.model.fixtures.ModelRegistryTestUtils.create;
import static dev.nokee.model.fixtures.ModelRegistryTestUtils.registry;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

@Subject(BridgedLanguageSourceSetProjection.class)
public class BridgedSourceSetTest extends LanguageSourceSetTester<LanguageSourceSet> {
	@Override
	public LanguageSourceSet createSubject() {
		return create(bridgeSourceSet(objectFactory().sourceDirectorySet("test", "test"),
			LanguageSourceSet.class)).as(LanguageSourceSet.class).get();
	}

	public LanguageSourceSet createSubject(SourceDirectorySet sourceSet) {
		return create(bridgeSourceSet(sourceSet,
			LanguageSourceSet.class)).as(LanguageSourceSet.class).get();
	}

	@Test
	void bridgedSourceSetValuesTakePrecedenceOverOurValue(@TempDir File temporaryDirectory) throws IOException {
		val a = new FileSystemWorkspace(temporaryDirectory);
		val sourceSet = ProjectTestUtils.objectFactory().sourceDirectorySet("java", "test");
		sourceSet.srcDir(newFiles(a.newDirectory("src/main/java")));
		val subject = createSubject(sourceSet);
		assertThat(subject.getAsFileTree(), containsInAnyOrder(a.file("src/main/java/f1"), a.file("src/main/java/f2")));
		assertThat(subject.getSourceDirectories(), containsInAnyOrder(a.file("src/main/java")));
	}

	@Test // TODO: ignore convention when source set already have data upon bridging
	void conventionOnNonEmptyBridgedSourceSetAreIgnored(@TempDir File temporaryDirectory) throws IOException {
		val a = new FileSystemWorkspace(temporaryDirectory);
		val sourceSet = ProjectTestUtils.objectFactory().sourceDirectorySet("java", "test");
		sourceSet.srcDir(newFiles(a.newDirectory("src/main/java")));
		val subject = createSubject(sourceSet).convention(a.file("srcs"));
		assertThat(subject.getAsFileTree(), containsInAnyOrder(a.file("src/main/java/f1"), a.file("src/main/java/f2")));
		assertThat(subject.getSourceDirectories(), containsInAnyOrder(a.file("src/main/java")));
		assertThat(sourceSet.getAsFileTree(), containsInAnyOrder(a.file("src/main/java/f1"), a.file("src/main/java/f2")));
		assertThat(sourceSet.getSourceDirectories(), containsInAnyOrder(a.file("src/main/java")));
	}

	@Test // TOOD: use convention when source set is empty upon bridging
	void conventionOnEmptyBridgedSourceSetIsUsed(@TempDir File temporaryDirectory) throws IOException {
		val a = new FileSystemWorkspace(temporaryDirectory);
		val sourceSet = ProjectTestUtils.objectFactory().sourceDirectorySet("java", "test");
		val subject = createSubject(sourceSet).convention(newFiles(a.newDirectory("srcs")));
		assertThat(subject.getAsFileTree(), containsInAnyOrder(a.file("srcs/f1"), a.file("srcs/f2")));
		assertThat(subject.getSourceDirectories(), containsInAnyOrder(a.file("srcs")));
		assertThat(sourceSet.getAsFileTree(), containsInAnyOrder(a.file("srcs/f1"), a.file("srcs/f2")));
		assertThat(sourceSet.getSourceDirectories(), containsInAnyOrder(a.file("srcs")));
	}

	// TODO: convention ignored when data is added after bridging but didn't have data when bridging
	@Test
	void ignoresConventionBridgedSourceSetIsUsed(@TempDir File temporaryDirectory) throws IOException {
		val a = new FileSystemWorkspace(temporaryDirectory);
		val sourceSet = ProjectTestUtils.objectFactory().sourceDirectorySet("java", "test");
		val subject = createSubject(sourceSet).convention(newFiles(a.newDirectory("srcs")));
		sourceSet.srcDir(newFiles(a.newDirectory("src/main/java")));

		assertThat(subject.getAsFileTree(), containsInAnyOrder(a.file("src/main/java/f1"), a.file("src/main/java/f2")));
		assertThat(subject.getSourceDirectories(), containsInAnyOrder(a.file("src/main/java")));
		assertThat(sourceSet.getAsFileTree(), containsInAnyOrder(a.file("src/main/java/f1"), a.file("src/main/java/f2")));
		assertThat(sourceSet.getSourceDirectories(), containsInAnyOrder(a.file("src/main/java")));
	}

	@Override
	public LanguageSourceSet createSubject(File baseDirectory) {
		val objectFactory = createRootProject(baseDirectory).getObjects();
		return create(registry(objectFactory), bridgeSourceSet(objectFactory.sourceDirectorySet("test", "test"), LanguageSourceSet.class)).as(LanguageSourceSet.class).get();
	}
}
