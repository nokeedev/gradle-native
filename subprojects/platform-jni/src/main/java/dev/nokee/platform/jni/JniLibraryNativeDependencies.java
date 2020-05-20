package dev.nokee.platform.jni;

import org.gradle.api.Action;
import org.gradle.api.artifacts.ExternalModuleDependency;

/**
 * Allows the native implementation dependencies of a JNI library to be specified.
 * It also allows native link only and runtime only dependencies of a JNI library to be specified.
 *
 * @since 0.4
 */
public interface JniLibraryNativeDependencies {
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
	 * @param action The action to run to configure the dependency.
	 */
	void nativeImplementation(Object notation, Action<? super ExternalModuleDependency> action);

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
	 * @param action The action to run to configure the dependency.
	 * @since 0.4
	 */
	void nativeLinkOnly(Object notation, Action<? super ExternalModuleDependency> action);

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
	 * @param action The action to run to configure the dependency.
	 */
	void nativeRuntimeOnly(Object notation, Action<? super ExternalModuleDependency> action);
}
