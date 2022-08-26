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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.file.Path;

import static dev.nokee.gradle.AdhocArtifactRepositoryFactory.forProject;
import static dev.nokee.internal.testing.util.ProjectTestUtils.createRootProject;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@ExtendWith(TestDirectoryExtension.class)
class AdhocArtifactRepositoryNameIntegrationTest {
	@TestDirectory Path testDirectory;
	AdhocArtifactRepository subject;

	@BeforeEach
	void setUp() {
		subject = forProject(createRootProject(testDirectory)).create();
	}

	@Test
	void canChangeRepositoryName() {
		subject.setName("my-new-name");
		assertThat(subject.getName(), equalTo("my-new-name"));
	}

	@Test
	void hasDefaultRepositoryName() {
		assertThat(subject.getName(), equalTo("adhoc"));
	}
}
