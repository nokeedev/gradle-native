package dev.nokee.platform.jni;

import org.gradle.api.Action;
import org.gradle.api.artifacts.ExternalModuleDependency;

/**
 * Allows the API and JVM implementation and native implementation dependencies of a JNI library to be specified.
 * It also allows JVM runtime only as well as native link only and runtime only dependencies of a JNI library to be specified.
 *
 * @since 0.1
 */
public interface JniLibraryDependencies extends JniLibraryNativeDependencies {
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
	 * @param action The action to run to configure the dependency.
	 */
	void api(Object notation, Action<? super ExternalModuleDependency> action);

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
	 * @param action The action to run to configure the dependency.
	 */
	void jvmImplementation(Object notation, Action<? super ExternalModuleDependency> action);

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
	 * @param action The action to run to configure the dependency.
	 */
	void jvmRuntimeOnly(Object notation, Action<? super ExternalModuleDependency> action);
}
