/*
 * Copyright 2022 the original author or authors.
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
package dev.nokee.gradle;

import lombok.val;
import net.nokeedev.testing.junit.jupiter.io.TestDirectory;
import net.nokeedev.testing.junit.jupiter.io.TestDirectoryExtension;
import org.gradle.api.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.Arrays;

import static dev.nokee.gradle.AdhocArtifactRepositoryFactory.forProject;
import static dev.nokee.gradle.AdhocArtifactRepositoryTestUtils.forModule;
import static dev.nokee.gradle.AdhocArtifactRepositoryTestUtils.query;
import static dev.nokee.gradle.AdhocArtifactRepositoryTestUtils.queryAndIgnoreFailures;
import static dev.nokee.internal.testing.FileSystemMatchers.anExistingDirectory;
import static dev.nokee.internal.testing.util.ProjectTestUtils.createRootProject;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

@ExtendWith({MockitoExtension.class, TestDirectoryExtension.class})
class AdhocArtifactRepositoryModuleVersionListerIntegrationTest {
	@TestDirectory Path testDirectory;
	Project project;
	@Mock AdhocComponentLister lister;
	AdhocArtifactRepository subject;

	@BeforeEach
	void setUp() {
		project = createRootProject(testDirectory);
		subject = forProject(project).create();
		project.getRepositories().add(subject);

		subject.getCacheDirectory().set(project.file("repo"));
		subject.setComponentVersionLister(lister);
	}

	@ParameterizedTest
	@ValueSource(strings = {"latest.release", "latest.integration", "1.+", "[2.0, 3.0["})
	void executesModuleVersionListerDuringDynamicComponentResolution(String dynamicVersion) {
		queryAndIgnoreFailures(project, "com.example:foo:" + dynamicVersion);
		Mockito.verify(lister).execute(argThat(forModule("com.example:foo")));
	}

	@Test
	void disallowChangesToModuleListerAfterRepositoryFirstQueried() {
		queryAndIgnoreFailures(project, "com.example:foo:4.+");
		val ex = assertThrows(IllegalStateException.class, () -> subject.setComponentVersionLister(mock(AdhocComponentLister.class)));
		assertThat(ex.getMessage(), equalTo("The component lister cannot be changed because repository was already queried."));
	}

	@Test
	void createsProvidedModuleVersions() {
		Mockito.doAnswer(it -> {
			val details = it.getArgument(0, AdhocComponentListerDetails.class);
			details.listed(Arrays.asList("1.0", "1.2", "1.1.1", "1.1"));
			return null;
		}).when(lister).execute(any());

		queryAndIgnoreFailures(project, "com.example:foo:1.+");
		assertThat(testDirectory.resolve("repo/com/example/foo/1.0"), anExistingDirectory());
		assertThat(testDirectory.resolve("repo/com/example/foo/1.1"), anExistingDirectory());
		assertThat(testDirectory.resolve("repo/com/example/foo/1.1.1"), anExistingDirectory());
		assertThat(testDirectory.resolve("repo/com/example/foo/1.2"), anExistingDirectory());
	}

	@Test
	void doesNotPropagateListerExceptions() {
		Mockito.doThrow(new RuntimeException("Internal exception!")).when(lister).execute(any());
		val ex = assertThrows(RuntimeException.class, () -> query(project, "com.example:far:2.+"));
		assertThat(ex.getMessage(), not(equalTo("Internal exception!")));
	}
}
