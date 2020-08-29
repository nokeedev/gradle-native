package dev.nokee.platform.jni;

import dev.nokee.platform.base.ComponentDependencies;
import dev.nokee.platform.base.DeclarableDependencyBucket;
import org.gradle.api.Action;
import org.gradle.api.artifacts.ExternalModuleDependency;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.artifacts.ProjectDependency;

/**
 * Allows the native implementation dependencies of a Java Native Interface (JNI) library to be specified.
 * It also allows native link only and runtime only dependencies of a JNI library to be specified.
 *
 * @since 0.5
 */
public interface JavaNativeInterfaceNativeComponentDependencies extends ComponentDependencies, JniLibraryNativeDependencies {
	/**
	 * Adds an native implementation dependency to this component.
	 * An implementation dependency is not visible to consumers that are compiled or linked against this component.
	 *
	 * @param notation The dependency notation, as per {@link org.gradle.api.artifacts.dsl.DependencyHandler#create(Object)}.
	 */
	void nativeImplementation(Object notation);

	/**
	 * Adds an native implementation dependency to this component.
	 * An implementation dependency is not visible to consumers that are compiled or linked against this component.
	 *
	 * @param notation The dependency notation, as per {@link org.gradle.api.artifacts.dsl.DependencyHandler#create(Object)}.
	 * @param action The action to run to configure the dependency (project dependencies are {@link ProjectDependency} and external dependencies are {@link ExternalModuleDependency}).
	 */
	void nativeImplementation(Object notation, Action<? super ModuleDependency> action);

	/**
	 * Adds an native link only dependency to this component.
	 * An link only dependency is not visible to consumers that are compiled or linked against this component.
	 *
	 * @param notation The dependency notation, as per {@link org.gradle.api.artifacts.dsl.DependencyHandler#create(Object)}.
	 */
	void nativeLinkOnly(Object notation);

	/**
	 * Adds an native link only dependency to this component.
	 * An link only dependency is not visible to consumers that are compiled or linked against this component.
	 *
	 * @param notation The dependency notation, as per {@link org.gradle.api.artifacts.dsl.DependencyHandler#create(Object)}.
	 * @param action The action to run to configure the dependency (project dependencies are {@link ProjectDependency} and external dependencies are {@link ExternalModuleDependency}).
	 * @since 0.4
	 */
	void nativeLinkOnly(Object notation, Action<? super ModuleDependency> action);

	/**
	 * Adds an native runtime only dependency to this component.
	 * An runtime only dependency is not visible to consumers that are running against this component.
	 *
	 * @param notation The dependency notation, as per {@link org.gradle.api.artifacts.dsl.DependencyHandler#create(Object)}.
	 */
	void nativeRuntimeOnly(Object notation);

	/**
	 * Adds an native runtime only dependency to this component.
	 * An runtime only dependency is visible only to consumers that are running against this component.
	 *
	 * @param notation The dependency notation, as per {@link org.gradle.api.artifacts.dsl.DependencyHandler#create(Object)}.
	 * @param action The action to run to configure the dependency (project dependencies are {@link ProjectDependency} and external dependencies are {@link ExternalModuleDependency}).
	 */
	void nativeRuntimeOnly(Object notation, Action<? super ModuleDependency> action);

	/**
	 * Returns the native implementation bucket of dependencies for this component.
	 *
	 * @return a {@link DeclarableDependencyBucket} representing the native implementation bucket of dependencies, never null.
	 */
	DeclarableDependencyBucket getNativeImplementation();

	/**
	 * Returns the native runtime only bucket of dependencies for this component.
	 *
	 * @return a {@link DeclarableDependencyBucket} representing the native runtime only implementation bucket of dependencies, never null.
	 */
	DeclarableDependencyBucket getNativeRuntimeOnly();

	/**
	 * Returns the native link only bucket of dependencies for this component.
	 *
	 * @return a {@link DeclarableDependencyBucket} representing the native link only implementation bucket of dependencies, never null.
	 */
	DeclarableDependencyBucket getNativeLinkOnly();
}
