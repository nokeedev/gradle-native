package dev.nokee.platform.base;

import org.gradle.api.Action;
import org.gradle.api.artifacts.ExternalModuleDependency;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.artifacts.ProjectDependency;

/**
 * The dependency buckets for an application component.
 *
 * @since 0.5
 */
public interface ApplicationComponentDependencies extends ComponentDependencies {
	/**
	 * Adds an implementation dependency to this component.
	 * An implementation dependency is not visible to consumers that are compiled against this component.
	 *
	 * @param notation The dependency notation, as per {@link org.gradle.api.artifacts.dsl.DependencyHandler#create(Object)}.
	 */
	void implementation(Object notation);

	/**
	 * Adds an implementation dependency to this component.
	 * An implementation dependency is not visible to consumers that are compiled against this component.
	 *
	 * @param notation The dependency notation, as per {@link org.gradle.api.artifacts.dsl.DependencyHandler#create(Object)}.
	 * @param action The action to run to configure the dependency (project dependencies are {@link ProjectDependency} and external dependencies are {@link ExternalModuleDependency}).
	 */
	void implementation(Object notation, Action<? super ModuleDependency> action);

	/**
	 * Returns the implementation bucket of dependencies for this component.
	 *
	 * @return a {@link DeclarableDependencyBucket} representing the implementation bucket of dependencies, never null.
	 */
	DeclarableDependencyBucket getImplementation();
}
