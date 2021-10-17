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
package dev.nokee.platform.base.internal.dependencies;

import com.google.common.testing.EqualsTester;
import dev.nokee.internal.testing.FileSystemWorkspace;
import dev.nokee.internal.testing.util.ConfigurationTestUtils;
import lombok.val;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.Sync;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.bundling.Zip;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

import static dev.nokee.internal.testing.ConfigurationMatchers.*;
import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static dev.nokee.internal.testing.util.ProjectTestUtils.rootProject;
import static dev.nokee.platform.base.internal.dependencies.ProjectConfigurationActions.*;
import static org.gradle.api.artifacts.type.ArtifactTypeDefinition.DIRECTORY_TYPE;
import static org.gradle.api.artifacts.type.ArtifactTypeDefinition.ZIP_TYPE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProjectConfigurationActions_ArtifactOfFileCollectionTest {
	private final TaskContainer taskContainer = rootProject().getTasks();
	@TempDir Path testDirectory;

	private static FileCollection aFileCollection(Path directory) throws IOException {
		val a = new FileSystemWorkspace(directory);
		return a.fileCollection(a.newFile("foo"), a.newFile("a/foo"));
	}

	private Configuration testConfiguration() throws IOException {
		return ConfigurationTestUtils.testConfiguration(using(taskContainer, artifactOf(aFileCollection(testDirectory))));
	}

	private Configuration testConfiguration(String name) throws IOException {
		return ConfigurationTestUtils.testConfiguration(name, using(taskContainer, artifactOf(aFileCollection(testDirectory))));
	}

	@Test
	void addsZipOfFileCollectionAsMainPublishArtifact() throws IOException {
		assertThat(testConfiguration(), hasPublishArtifact(ofType(ZIP_TYPE)));
	}

	@Test
	void usesConfigurationNameAsClassifier() throws IOException {
		assertThat(testConfiguration("test"), hasPublishArtifact(ofClassifier("test")));
	}

	@Test
	void infersClassifierByRemovingElementsSuffixFromConfigurationName() throws IOException {
		assertThat(testConfiguration("testElements"), hasPublishArtifact(ofClassifier("test")));
	}

	@Test
	void createsZipTask() throws IOException {
		testConfiguration("testElements");
		assertThat(taskContainer, hasItem(allOf(named("zipTestElements"), isA(Zip.class))));
	}

	@Test
	void createsStageTask() throws IOException {
		testConfiguration("testElements");
		assertThat(taskContainer, hasItem(allOf(named("stageTestElements"), isA(Sync.class))));
	}

	@Test
	void addsDirectoryOfFileCollectionAsPublishArtifactVariant() throws IOException {
		assertThat(testConfiguration(), hasOutgoingVariant(allOf(named("directory"), hasPublishArtifact(ofType(DIRECTORY_TYPE)))));
	}

	@Test
	void alwaysThrowsExceptionWhenAsserting() {
		assertThrows(UnsupportedOperationException.class,
			() -> assertConfigured(testConfiguration(), artifactOf(rootProject().files())));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() throws IOException {
		val fileCollectionA = aFileCollection(testDirectory.resolve("testA"));
		val fileCollectionB = aFileCollection(testDirectory.resolve("testB"));
		new EqualsTester()
			.addEqualityGroup(artifactOf(fileCollectionA), artifactOf(fileCollectionA))
			.addEqualityGroup(artifactOf(fileCollectionB))
			.testEquals();
	}

	@Test
	void checkToString() throws IOException {
		assertThat(artifactOf(aFileCollection(testDirectory)),
			hasToString("ProjectConfigurationActions.artifactOf(file collection)"));
	}
}
