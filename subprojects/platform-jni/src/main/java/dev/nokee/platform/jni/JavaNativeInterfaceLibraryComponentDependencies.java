package dev.nokee.platform.jni;

import dev.nokee.platform.base.ComponentDependencies;
import dev.nokee.platform.base.DependencyBucket;
import org.gradle.api.Action;
import org.gradle.api.artifacts.ExternalModuleDependency;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.artifacts.ProjectDependency;

/**
 * Allows the API, JVM implementation and native implementation dependencies of a Java Native Interface (JNI) library to be specified.
 * It also allows JVM runtime only as well as native link only and runtime only dependencies of a JNI library to be specified.
 *
 * @since 0.5
 */
public interface JavaNativeInterfaceLibraryComponentDependencies extends JavaNativeInterfaceNativeComponentDependencies, ComponentDependencies, JniLibraryDependencies {
	/**
	 * Adds an JVM API dependency to this library. An API dependency is made visible to consumers that are compiled against this component.
	 *
	 * @param notation The dependency notation, as per {@link org.gradle.api.artifacts.dsl.DependencyHandler#create(Object)}.
	 */
	void api(Object notation);

	/**
	 * Adds an JVM API dependency to this library. An API dependency is made visible to consumers that are compiled against this component.
	 *
	 * @param notation The dependency notation, as per {@link org.gradle.api.artifacts.dsl.DependencyHandler#create(Object)}.
	 * @param action The action to run to configure the dependency (project dependencies are {@link ProjectDependency} and external dependencies are {@link ExternalModuleDependency}).
	 */
	void api(Object notation, Action<? super ModuleDependency> action);

	/**
	 * Adds an JVM implementation dependency to this library. An implementation dependency is not visible to consumers that are compiled against this component.
	 *
	 * @param notation The dependency notation, as per {@link org.gradle.api.artifacts.dsl.DependencyHandler#create(Object)}.
	 */
	void jvmImplementation(Object notation);

	/**
	 * Adds an JVM implementation dependency to this library.
	 * An implementation dependency is not visible to consumers that are compiled against this component.
	 *
	 * @param notation The dependency notation, as per {@link org.gradle.api.artifacts.dsl.DependencyHandler#create(Object)}.
	 * @param action The action to run to configure the dependency (project dependencies are {@link ProjectDependency} and external dependencies are {@link ExternalModuleDependency}).
	 */
	void jvmImplementation(Object notation, Action<? super ModuleDependency> action);

	/**
	 * Adds an JVM runtime only dependency to this library.
	 * An implementation dependency is only visible to consumers that are running against this component.
	 *
	 * @param notation The dependency notation, as per {@link org.gradle.api.artifacts.dsl.DependencyHandler#create(Object)}.
	 */
	void jvmRuntimeOnly(Object notation);

	/**
	 * Adds an JVM runtime only dependency to this library.
	 * An implementation dependency is only visible to consumers that are running against this component.
	 *
	 * @param notation The dependency notation, as per {@link org.gradle.api.artifacts.dsl.DependencyHandler#create(Object)}.
	 * @param action The action to run to configure the dependency (project dependencies are {@link ProjectDependency} and external dependencies are {@link ExternalModuleDependency}).
	 */
	void jvmRuntimeOnly(Object notation, Action<? super ModuleDependency> action);

	/**
	 * Returns the JVM runtime only bucket of dependencies for this component.
	 *
	 * @return a {@link DependencyBucket} representing the JVM runtime only implementation bucket of dependencies, never null.
	 */
	DependencyBucket getJvmRuntimeOnly();

	/**
	 * Returns the JVM implementation bucket of dependencies for this component.
	 *
	 * @return a {@link DependencyBucket} representing the JVM implementation bucket of dependencies, never null.
	 */
	DependencyBucket getJvmImplementation();

	/**
	 * Returns the api bucket of dependencies for this component.
	 *
	 * @return a {@link DependencyBucket} representing the api bucket of dependencies, never null.
	 */
	DependencyBucket getApi();
}
