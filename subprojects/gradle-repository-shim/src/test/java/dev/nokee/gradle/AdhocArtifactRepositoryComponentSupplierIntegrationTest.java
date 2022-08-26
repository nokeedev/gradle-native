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
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;

import static dev.nokee.gradle.AdhocArtifactRepositoryFactory.forProject;
import static dev.nokee.gradle.AdhocArtifactRepositoryTestUtils.forId;
import static dev.nokee.gradle.AdhocArtifactRepositoryTestUtils.query;
import static dev.nokee.internal.testing.util.ProjectTestUtils.createRootProject;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

@ExtendWith({MockitoExtension.class, TestDirectoryExtension.class})
class AdhocArtifactRepositoryComponentSupplierIntegrationTest {
	@TestDirectory Path testDirectory;
	Project project;
	@Mock AdhocComponentSupplier supplier;
	AdhocArtifactRepository subject;

	@BeforeEach
	void setUp() {
		project = createRootProject(testDirectory);
		subject = forProject(project).create();
		project.getRepositories().add(subject);

		subject.getCacheDirectory().set(project.file("test"));
		subject.setComponentSupplier(supplier);
	}

	@Test
	void executesComponentSupplierRuleDuringComponentResolution() {
		query(project, "com.example:foo:4.2");
		Mockito.verify(supplier).execute(argThat(forId("com.example:foo:4.2")));
	}

	@Test
	void disallowChangesToComponentSupplierAfterRepositoryFirstQueried() {
		query(project, "com.example:foo:4.2");
		val ex = assertThrows(IllegalStateException.class, () -> subject.setComponentSupplier(mock(AdhocComponentSupplier.class)));
		assertThat(ex.getMessage(), equalTo("The component supplier cannot be changed because repository was already queried."));
	}
}
