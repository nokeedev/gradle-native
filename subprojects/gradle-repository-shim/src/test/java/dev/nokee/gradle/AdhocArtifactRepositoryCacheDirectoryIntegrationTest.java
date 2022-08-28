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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static dev.nokee.gradle.AdhocArtifactRepositoryTestUtils.queryAndIgnoreFailures;
import static dev.nokee.internal.testing.FileSystemMatchers.anExistingFile;
import static dev.nokee.internal.testing.GradleProviderMatchers.changesDisallowed;
import static dev.nokee.internal.testing.util.ProjectTestUtils.createRootProject;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.createDirectories;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;

@ExtendWith(TestDirectoryExtension.class)
class AdhocArtifactRepositoryCacheDirectoryIntegrationTest {
	@TestDirectory Path testDirectory;
	Project project;
	AdhocArtifactRepository subject;

	@BeforeEach
	void setUp() throws IOException {
		project = createRootProject(testDirectory);
		subject = AdhocArtifactRepositoryFactory.forProject(project).create();
		project.getRepositories().add(subject);

		subject.getCacheDirectory().set(testDirectory.resolve("repo").toFile());
		subject.setComponentSupplier(new AdhocComponentSupplier() {
			@Override
			public void execute(AdhocComponentSupplierDetails details) {
				if (details.getId().getModule().equals("foo")) {
					details.file("foo-1.0.txt", it -> {
						try {
							it.write("content...".getBytes(UTF_8));
						} catch (IOException e) {
							throw new UncheckedIOException(e);
						}
					});
				} else if (details.getId().getModule().equals("bar")) {
					details.file("bar-1.2.txt", it -> {
						try {
							it.write("content...".getBytes(UTF_8));
						} catch (IOException e) {
							throw new UncheckedIOException(e);
						}
					});
				}
			}
		});

		val componentPath = createDirectories(testDirectory.resolve("repo/com/example/old-module/1.3"));
		Files.createFile(componentPath.resolve("old-module-1.3.txt"));
	}

	@Test
	void disallowChangesToCacheDirectoryAfterRepositoryFirstQueried() {
		queryAndIgnoreFailures(project, "com.example:foo:1.0");
		assertThat(subject.getCacheDirectory(), changesDisallowed());
	}

	@Test
	void deletesRepositoryOnFirstQuery() {
		queryAndIgnoreFailures(project, "com.example:bar:1.2");
		assertThat(testDirectory.resolve("repo/com/example/old-module/1.3/old-module-1.3.txt"), not(anExistingFile()));
	}

	@Test
	void doesNotDeleteRepositoryOnSubsequentQuery() {
		queryAndIgnoreFailures(project, "com.example:foo:1.0");
		queryAndIgnoreFailures(project, "com.example:bar:1.2");
		assertThat(testDirectory.resolve("repo/com/example/foo/1.0/foo-1.0.txt"), anExistingFile());
	}
}
