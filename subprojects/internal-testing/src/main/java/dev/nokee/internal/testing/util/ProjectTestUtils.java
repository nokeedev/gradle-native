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
package dev.nokee.internal.testing.util;

import dev.gradleplugins.grava.testing.file.TestNameTestDirectoryProvider;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.testfixtures.ProjectBuilder;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Test utilities to access various Gradle services useful during testing as well as Project instances.
 */
public final class ProjectTestUtils {
	private static final String CLEANUP_THREAD_NAME = "project-test-utils-cleanup";
	private static final AtomicBoolean SHUTDOWN_REGISTERED = new AtomicBoolean();
	private static final List<TestNameTestDirectoryProvider> PROJECT_DIRECTORIES_TO_CLEANUP = Collections.synchronizedList(new LinkedList<>());
	private static Project _use_project_method = null;
	private ProjectTestUtils() {}

	private static void maybeRegisterCleanup() {
		if (SHUTDOWN_REGISTERED.compareAndSet(false, true)) {
			Runtime.getRuntime().addShutdownHook(new Thread(ProjectTestUtils::cleanup, CLEANUP_THREAD_NAME));
		}
	}

	private static Project project() {
		if (_use_project_method == null) {
			_use_project_method = rootProject();
		}
		return _use_project_method;
	}

	/**
	 * Returns an functional {@link ObjectFactory} instance.
	 *
	 * The project associated to the returned ObjectFactory is unspecified meaning tests should not depend on it.
	 * This particularity means that each file related operation that would spawn from this ObjectFactory will be resolved from an unspecified, but valid, file system location.
	 * If the file resolution needs to be specified, create a {@link Project} instance using {@link #createRootProject(File)}.
	 *
	 * @return a {@link ObjectFactory} instance, never null
	 */
	public static ObjectFactory objectFactory() {
		return project().getObjects();
	}

	/**
	 * Returns a functional {@link ProviderFactory} instance.
	 *
	 * @return a {@link ProviderFactory} instance, never null
	 */
	public static ProviderFactory providerFactory() {
		return project().getProviders();
	}

	/**
	 * Creates a {@link Dependency} instance for the specified notation.
	 *
	 * @param notation  dependency notation, must not be null
	 * @return a {@link Dependency} instance for the notation, never null
	 */
	public static Dependency createDependency(Object notation) {
		return project().getDependencies().create(notation);
	}

	/**
	 * Creates a new root project instance.
	 *
	 * @return a new {@link Project} instance, never null
	 */
	public static Project rootProject() {
		maybeRegisterCleanup();
		val testDirectory = new TestNameTestDirectoryProvider(ProjectTestUtils.class);
		PROJECT_DIRECTORIES_TO_CLEANUP.add(testDirectory);
		return ProjectBuilder.builder().withProjectDir(testDirectory.getTestDirectory().toFile()).build();
	}

	/**
	 * Creates a new root project instance for the given project directory.
	 *
	 * @param rootDirectory  a project directory for the root project, must not be null
	 * @return a new {@link Project} instance, never null
	 */
	public static Project createRootProject(File rootDirectory) {
		return ProjectBuilder
			.builder()
			.withProjectDir(rootDirectory)
			.build();
	}

	/**
	 * Creates a new root project instance for the given project directory.
	 *
	 * @param rootDirectory  a project directory for the root project, must not be null
	 * @return a new {@link Project} instance, never null
	 */
	public static Project createRootProject(Path rootDirectory) {
		return createRootProject(rootDirectory.toFile());
	}

	/**
	 * Creates a child project instance with the specified parent project.
	 * The child project name and directory defaults to {@literal test} and <pre>${parent.projectDir}/test</pre> respectively.
	 *
	 * @param parent  the parent project for the child project, must not be null
	 * @return a new child {@link Project} instance of the specified parent project, never null
	 */
	public static Project createChildProject(Project parent) {
		return createChildProject(parent, "test");
	}

	/**
	 * Creates a child project instance with the specified parent project and name.
	 * The child project directory defaults to <pre>${parent.projectDir}/${name}</pre>.
	 *
	 * @param parent  the parent project for the child project, must not be null
	 * @param name  the child project name, must not be null
	 * @return a new child {@link Project} instance of the specified parent project, never null
	 */
	public static Project createChildProject(Project parent, String name) {
		return createChildProject(parent, name, new File(parent.getProjectDir(), name));
	}

	/**
	 * Creates a child project instance with the specified parent project, name and directory.
	 *
	 * @param parent  the parent project for the child project, must not be null
	 * @param name  the child project name, must not be null
	 * @param projectDirectory  the child project directory, must not be null
	 * @return a new child {@link Project} instance of the specified parent project, never null
	 */
	public static Project createChildProject(Project parent, String name, File projectDirectory) {
		return ProjectBuilder
			.builder()
			.withName(name)
			.withParent(parent)
			.withProjectDir(toCanonicalFile(projectDirectory))
			.build();
	}

	/**
	 * Creates a child project instance with the specified parent project, name and directory.
	 *
	 * @param parent  the parent project for the child project, must not be null
	 * @param name  the child project name, must not be null
	 * @param projectDirectory  the child project directory, must not be null
	 * @return a new child {@link Project} instance of the specified parent project, never null
	 */
	public static Project createChildProject(Project parent, String name, Path projectDirectory) {
		return createChildProject(parent, name, projectDirectory.toFile());
	}

	/**
	 * Force the evaluation of the specified Gradle project.
	 * It will execute all {@link Project#afterEvaluate(Action)} configuration action.
	 *
	 * Note: It is generally preferable to write functional test using Gradle Runner Kit to test after evaluate behavior.
	 *
	 * Implementation Note: It uses a call to an internal Gradle API, e.g. {@link ProjectInternal#evaluate()}.
	 *
	 * @param project  the project to evaluate, must not be null
	 * @return the specified project, never null
	 */
	public static Project evaluate(Project project) {
		return ((ProjectInternal) project).evaluate();
	}

	private static File toCanonicalFile(File file) {
		try {
			return file.getCanonicalFile();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	/**
	 * Force cleanup of the temporary project directories.
	 */
	public static void cleanup() {
		try {
			synchronized (PROJECT_DIRECTORIES_TO_CLEANUP) {
				try {
					for (TestNameTestDirectoryProvider testDirectory : PROJECT_DIRECTORIES_TO_CLEANUP) {
						testDirectory.cleanup();
					}
				} finally {
					PROJECT_DIRECTORIES_TO_CLEANUP.clear();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
