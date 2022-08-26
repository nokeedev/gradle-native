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

import net.nokeedev.testing.junit.jupiter.io.TestDirectory;
import net.nokeedev.testing.junit.jupiter.io.TestDirectoryExtension;
import org.gradle.api.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;

import static dev.nokee.gradle.AdhocArtifactRepositoryFactory.forProject;
import static dev.nokee.gradle.AdhocArtifactRepositoryTestUtils.forId;
import static dev.nokee.gradle.AdhocArtifactRepositoryTestUtils.forModule;
import static dev.nokee.gradle.AdhocArtifactRepositoryTestUtils.query;
import static dev.nokee.internal.testing.util.ProjectTestUtils.createRootProject;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

@ExtendWith({MockitoExtension.class, TestDirectoryExtension.class})
class AdhocArtifactRepositoryContentFilterIntegrationTest {
	@TestDirectory Path testDirectory;
	Project project;
	@Mock AdhocComponentSupplier supplier;
	@Mock AdhocComponentLister lister;

	@BeforeEach
	void setUp() {
		project = createRootProject(testDirectory);
		project.getRepositories().add(forProject(project).create());
		project.getRepositories().withType(AdhocArtifactRepository.class).configureEach(repo -> {
			repo.getCacheDirectory().set(project.file("repo"));
			repo.setComponentSupplier(supplier);
			repo.setComponentVersionLister(lister);
			repo.content(it -> it.includeGroup("dev.example"));
		});
	}

	@Test
	void doesNotQueryRepositoryOfNonIncludedContent() {
		query(project, "com.example:foo:5.4");
		Mockito.verify(supplier, never()).execute(any());
		Mockito.verify(lister, never()).execute(any());
	}

	@Test
	void queryRepositoryOfIncludedContent() {
		query(project, "dev.example:foo:5.4");
		Mockito.verify(supplier, only()).execute(argThat(forId("dev.example:foo:5.4")));
		Mockito.verify(lister, never()).execute(any());
	}

	@Test
	void queryRepositoryOfIncludedDynamicContent() {
		query(project, "dev.example:foo:5.+");
		Mockito.verify(lister, only()).execute(argThat(forModule("dev.example:foo")));
	}
}
