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
package dev.nokee.buildadapter.xcode.internal;

import com.google.common.collect.ImmutableMap;
import dev.nokee.buildadapter.xcode.internal.plugins.RedirectProjectBuildDirectoryToRootBuildDirectory;
import dev.nokee.buildadapter.xcode.internal.plugins.RedirectProjectBuildDirectoryToRootBuildDirectory.HashFunction;
import dev.nokee.internal.testing.util.ProjectTestUtils;
import net.nokeedev.testing.junit.jupiter.io.TestDirectory;
import net.nokeedev.testing.junit.jupiter.io.TestDirectoryExtension;
import org.checkerframework.checker.units.qual.A;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import static dev.nokee.internal.testing.FileSystemMatchers.aFile;
import static dev.nokee.internal.testing.FileSystemMatchers.withAbsolutePath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith({TestDirectoryExtension.class, MockitoExtension.class})
class RedirectProjectBuildDirectoryToRootBuildDirectoryRestoreFromCacheIntegrationTest {
	static HashFunction algorithm = Mockito.mock(HashFunction.class);
	static Action<Project> subject;
	@TestDirectory static Path testDirectory;
	static Project rootProject;
	static Project projectA;
	static Project projectB;
	static Project projectB_A;

	@BeforeAll
	static void setup() throws IOException {
		save(testDirectory.resolve("build/subprojects/mapping.ser"), ImmutableMap.of(":A", "A-16", ":B", "B-1g", ":B:A", "A-1q"));
		subject = new RedirectProjectBuildDirectoryToRootBuildDirectory(algorithm);
		rootProject = ProjectTestUtils.createRootProject(testDirectory);
		projectA = ProjectTestUtils.createChildProject(rootProject, "A");
		projectB = ProjectTestUtils.createChildProject(rootProject, "B");
		projectB_A = ProjectTestUtils.createChildProject(projectB, "A");

		subject.execute(rootProject);
	}

	@Test
	void doesNotUseSameBuildDirectoryBetweenAmbiguousProject() {
		assertThat(projectA.getBuildDir(), not(equalTo(projectB_A.getBuildDir())));
	}

	@Test
	void reuseCachedBuildDirectories() {
		verify(algorithm, never()).hash(any()); // all build path restore from cache
	}

	@Test
	void hasProject_A_BuildDirectory() {
		assertThat(projectA.getBuildDir(), aFile(withAbsolutePath(endsWith("/build/subprojects/A-16"))));
	}

	@Test
	void hasProject_B_BuildDirectory() {
		assertThat(projectB.getBuildDir(), aFile(withAbsolutePath(endsWith("/build/subprojects/B-1g"))));
	}

	@Test
	void hasProject_A_B_BuildDirectory() {
		assertThat(projectB_A.getBuildDir(), aFile(withAbsolutePath(endsWith("/build/subprojects/A-1q"))));
	}

	private static void save(Path cacheFile, Map<String, String> cache) throws IOException {
		Files.createDirectories(cacheFile.getParent());
		try (final ObjectOutputStream outStream = new ObjectOutputStream(Files.newOutputStream(cacheFile))) {
			outStream.writeObject(new LinkedHashMap<>(cache));
		}
	}
}
