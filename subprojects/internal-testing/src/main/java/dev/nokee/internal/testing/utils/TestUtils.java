package dev.nokee.internal.testing.utils;

import org.gradle.api.Project;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.testfixtures.ProjectBuilder;

import java.io.File;

/**
 * Test utilities to access various Gradle services useful during testing.
 */
public final class TestUtils {
	private static Project _use_project_method = null;
	private TestUtils() {}

	private static Project project() {
		if (_use_project_method == null) {
			_use_project_method = ProjectBuilder.builder().build();
		}
		return _use_project_method;
	}

	public static ObjectFactory objectFactory() {
		return project().getObjects();
	}

	public static ProviderFactory providerFactory() {
		return project().getProviders();
	}

	public static Project rootProject() {
		return ProjectBuilder.builder().build();
	}

	public static Project createRootProject(File rootDirectory) {
		return ProjectBuilder
			.builder()
			.withProjectDir(rootDirectory)
			.build();
	}

	public static Project createChildProject(Project parent) {
		return ProjectBuilder
			.builder()
			.withParent(parent)
			.build();
	}

	public static Project createChildProject(Project parent, String name) {
		return ProjectBuilder
			.builder()
			.withName(name)
			.withParent(parent)
			.build();
	}

	public static Project createChildProject(Project parent, String name, File projectDirectory) {
		return ProjectBuilder
			.builder()
			.withName(name)
			.withParent(parent)
			.withProjectDir(projectDirectory)
			.build();
	}

	public static Project evaluate(Project project) {
		return ((ProjectInternal) project).evaluate();
	}
}
