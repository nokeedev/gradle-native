package dev.nokee.platform.base;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.gradle.api.Action;
import org.gradle.api.artifacts.ExternalModuleDependency;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.util.ConfigureUtil;

/**
 * The dependency buckets for a library component.
 *
 * @since 0.5
 */
public interface LibraryComponentDependencies extends ComponentDependencies {
	/**
	 * Adds an API dependency to this library. An API dependency is made visible to consumers that are compiled against this component.
	 *
	 * @param notation The dependency notation, as per {@link org.gradle.api.artifacts.dsl.DependencyHandler#create(Object)}.
	 */
	void api(Object notation);

	/**
	 * Adds an API dependency to this library. An API dependency is made visible to consumers that are compiled against this component.
	 *
	 * @param notation The dependency notation, as per {@link org.gradle.api.artifacts.dsl.DependencyHandler#create(Object)}.
	 * @param action The action to run to configure the dependency (project dependencies are {@link ProjectDependency} and external dependencies are {@link ExternalModuleDependency}).
	 */
	void api(Object notation, Action<? super ModuleDependency> action);

	/**
	 * Adds an API dependency to this library. An API dependency is made visible to consumers that are compiled against this component.
	 *
	 * @param notation The dependency notation, as per {@link org.gradle.api.artifacts.dsl.DependencyHandler#create(Object)}.
	 * @param closure The closure to run to configure the dependency (project dependencies are {@link ProjectDependency} and external dependencies are {@link ExternalModuleDependency}).
	 */
	default void api(Object notation, @DelegatesTo(value = ModuleDependency.class, strategy = Closure.DELEGATE_FIRST) Closure<Void> closure) {
		api(notation, ConfigureUtil.configureUsing(closure));
	}

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
	 * Adds an implementation dependency to this component.
	 * An implementation dependency is not visible to consumers that are compiled against this component.
	 *
	 * @param notation The dependency notation, as per {@link org.gradle.api.artifacts.dsl.DependencyHandler#create(Object)}.
	 * @param closure The closure to run to configure the dependency (project dependencies are {@link ProjectDependency} and external dependencies are {@link ExternalModuleDependency}).
	 */
	default void implementation(Object notation, @DelegatesTo(value = ModuleDependency.class, strategy = Closure.DELEGATE_FIRST) Closure<Void> closure) {
		implementation(notation, ConfigureUtil.configureUsing(closure));
	}

	/**
	 * Returns the implementation bucket of dependencies for this component.
	 *
	 * @return a {@link DependencyBucket} representing the implementation bucket of dependencies, never null.
	 */
	DependencyBucket getImplementation();

	/**
	 * Returns the api bucket of dependencies for this component.
	 *
	 * @return a {@link DependencyBucket} representing the api bucket of dependencies, never null.
	 */
	DependencyBucket getApi();
}
