package dev.nokee.platform.jni;

import org.gradle.api.Action;
import org.gradle.api.artifacts.ExternalModuleDependency;

/**
 * Allows the API and JVM implementation and native implementation dependencies of a JNI library to be specified.
 *
 * @since 0.1
 */
public interface JniLibraryDependencies {
	/**
	 * Adds an JVM API dependency to this library. An API dependency is made visible to consumers that are compiled against this component.
	 *
	 * @param notation The dependency notation, as per {@link org.gradle.api.artifacts.dsl.DependencyHandler#create(Object)}.
	 * @since 0.1
	 */
	void api(Object notation);

	/**
	 * Adds an JVM API dependency to this library. An API dependency is made visible to consumers that are compiled against this component.
	 *
	 * @param notation The dependency notation, as per {@link org.gradle.api.artifacts.dsl.DependencyHandler#create(Object)}.
	 * @param action The action to run to configure the dependency.
	 * @since 0.1
	 */
	void api(Object notation, Action<? super ExternalModuleDependency> action);

	/**
	 * Adds an JVM implementation dependency to this library. An implementation dependency is not visible to consumers that are compiled against this component.
	 *
	 * @param notation The dependency notation, as per {@link org.gradle.api.artifacts.dsl.DependencyHandler#create(Object)}.
	 * @since 0.1
	 */
	void jvmImplementation(Object notation);

	/**
	 * Adds an JVM implementation dependency to this library. An implementation dependency is not visible to consumers that are compiled against this component.
	 *
	 * @param notation The dependency notation, as per {@link org.gradle.api.artifacts.dsl.DependencyHandler#create(Object)}.
	 * @param action The action to run to configure the dependency.
	 * @since 0.1
	 */
	void jvmImplementation(Object notation, Action<? super ExternalModuleDependency> action);

	/**
	 * Adds an native implementation dependency to this component. An implementation dependency is not visible to consumers that are compiled against this component.
	 *
	 * @param notation The dependency notation, as per {@link org.gradle.api.artifacts.dsl.DependencyHandler#create(Object)}.
	 * @since 0.1
	 */
	void nativeImplementation(Object notation);

	/**
	 * Adds an native implementation dependency to this component. An implementation dependency is not visible to consumers that are compiled against this component.
	 *
	 * @param notation The dependency notation, as per {@link org.gradle.api.artifacts.dsl.DependencyHandler#create(Object)}.
	 * @param action The action to run to configure the dependency.
	 * @since 0.1
	 */
	void nativeImplementation(Object notation, Action<? super ExternalModuleDependency> action);
}
