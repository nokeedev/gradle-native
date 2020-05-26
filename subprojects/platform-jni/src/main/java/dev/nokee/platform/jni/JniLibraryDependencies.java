package dev.nokee.platform.jni;

import dev.nokee.platform.base.ComponentDependencies;
import org.gradle.api.Action;
import org.gradle.api.artifacts.ExternalModuleDependency;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.artifacts.ProjectDependency;

/**
 * Allows the API and JVM implementation and native implementation dependencies of a JNI library to be specified.
 * It also allows JVM runtime only as well as native link only and runtime only dependencies of a JNI library to be specified.
 *
 * @since 0.1
 */
public interface JniLibraryDependencies extends JniLibraryNativeDependencies, ComponentDependencies {
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
	 * {@inheritDoc}
	 */
	@Override
	void nativeImplementation(Object notation);

	/**
	 * {@inheritDoc}
	 */
	@Override
	void nativeImplementation(Object notation, Action<? super ModuleDependency> action);

	/**
	 * {@inheritDoc}
	 */
	@Override
	void nativeLinkOnly(Object notation);

	/**
	 * {@inheritDoc}
	 */
	@Override
	void nativeLinkOnly(Object notation, Action<? super ModuleDependency> action);

	/**
	 * {@inheritDoc}
	 */
	@Override
	void nativeRuntimeOnly(Object notation);

	/**
	 * {@inheritDoc}
	 */
	@Override
	void nativeRuntimeOnly(Object notation, Action<? super ModuleDependency> action);
}
